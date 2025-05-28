package com.englearn;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Englearning implements ModInitializer {
	public static final String MOD_ID = "englearning";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier WORD_SELECTION_PACKET = new Identifier(MOD_ID, "word_selection");
	public static final Identifier WORD_ANSWER_PACKET = new Identifier(MOD_ID, "word_answer");
	public static final Identifier CANCEL_CHALLENGE_PACKET = new Identifier(MOD_ID, "cancel_challenge");

	private static final Map<String, List<Word>> wordLists = new ConcurrentHashMap<>(); // 存储多词库
	private static final Map<String, Map<String, Word>> wordMaps = new ConcurrentHashMap<>(); // 词库索引
	private static final List<String> availableWordLists = new ArrayList<>(); // 可用词库

	private static final Map<UUID, AttackContext> pendingAttacks = new ConcurrentHashMap<>();
	private static final Random random = new Random();

	private static final Map<UUID, Integer> playerCorrectCount = new ConcurrentHashMap<>();
	public static final int DEFAULT_REPEAT_INTERVAL = 15;

	@Override
	public void onInitialize() {
		LOGGER.info("English Learning Mod initialized!");
		loadWords();
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
		ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
		registerNetworking();
		registerEvents();
		registerCommands();
	}

	private void onServerStarted(MinecraftServer server) {
		PlayerDataManager.initialize(server);
		LOGGER.info("Player data manager initialized.");
	}

	private void onServerStopped(MinecraftServer server) {
		PlayerDataManager.saveAllPlayerData();
		playerCorrectCount.clear();
		for (AttackContext context : pendingAttacks.values()) {
			if (context.target instanceof LivingEntity livingTarget) {
				livingTarget.removeStatusEffect(StatusEffects.SLOWNESS);
				livingTarget.removeStatusEffect(StatusEffects.BLINDNESS);
				LOGGER.info("Server stopped: Removed slowness and blindness from {}.", livingTarget.getName().getString());
			}
		}
		pendingAttacks.clear();
		LOGGER.info("Player data saved and temporary data cleaned up on server shutdown.");
	}

	private void loadWords() {
		List<String> wordListNames = List.of("kaoyan", "gaozhong", "CET4", "CET6", "TOEFL", "SAT");
		Gson gson = new Gson();
		Type listType = new TypeToken<List<Word>>() {}.getType();

		for (String wordListName : wordListNames) {
			try {
				String path = "/assets/englearning/data/" + wordListName + ".json";
				InputStream inputStream = getClass().getResourceAsStream(path);
				if (inputStream == null) {
					LOGGER.error("Could not find {}.json at {}", wordListName, path);
					continue;
				}
				List<Word> words = gson.fromJson(new InputStreamReader(inputStream, "UTF-8"), listType);
				if (words == null || words.isEmpty()) {
					LOGGER.warn("{}.json is empty or invalid", wordListName);
					continue;
				}
				Map<String, Word> wordMap = new ConcurrentHashMap<>();
				for (Word word : words) {
					wordMap.put(word.getWord(), word);
				}
				wordLists.put(wordListName, words);
				wordMaps.put(wordListName, wordMap);
				availableWordLists.add(wordListName);
				LOGGER.info("Loaded {} words from {}.json", words.size(), wordListName);
				inputStream.close();
			} catch (Exception e) {
				LOGGER.error("Error loading {}.json", wordListName, e);
			}
		}

		if (wordLists.isEmpty()) {
			LOGGER.error("No word lists loaded. Adding default words.");
			List<Word> defaultWords = new ArrayList<>();
			defaultWords.add(new Word("hello", Collections.singletonList(new Word.Translation("你好", "interj")), new ArrayList<>()));
			defaultWords.add(new Word("world", Collections.singletonList(new Word.Translation("世界", "n")), new ArrayList<>()));
			defaultWords.add(new Word("attack", Collections.singletonList(new Word.Translation("攻击", "v")), new ArrayList<>()));
			defaultWords.add(new Word("damage", Collections.singletonList(new Word.Translation("伤害", "n")), new ArrayList<>()));
			Map<String, Word> defaultMap = new ConcurrentHashMap<>();
			for (Word word : defaultWords) {
				defaultMap.put(word.getWord(), word);
			}
			wordLists.put("default", defaultWords);
			wordMaps.put("default", defaultMap);
			availableWordLists.add("default");
		}
	}

	public static Word getWordByString(String wordStr, String wordListName) {
		Map<String, Word> wordMap = wordMaps.getOrDefault(wordListName, wordMaps.get("default"));
		Word word = wordMap.get(wordStr);
		if (word == null) {
			LOGGER.warn("Word not found in {}: {}", wordListName, wordStr);
		}
		return word;
	}

	private void grantReward(ServerPlayerEntity player, int correctCount) {
		UUID playerId = player.getUuid();
		Set<Integer> claimedMilestones = PlayerDataManager.getRewardMilestones(playerId);
		ItemStack reward = null;
		String rewardMessage = null;

		if (correctCount % 10 == 0) {
			switch (correctCount) {
				case 10:
					if (!claimedMilestones.contains(10)) {
						player.addExperienceLevels(1);
						rewardMessage = "§a达成10单词！获得经验等级+1！";
						claimedMilestones.add(10);
					}
					break;
				case 20:
					if (!claimedMilestones.contains(20)) {
						reward = new ItemStack(Items.IRON_INGOT, 5);
						rewardMessage = "§a达成20单词！获得铁锭×5！";
						claimedMilestones.add(20);
					}
					break;
				case 30:
					if (!claimedMilestones.contains(30)) {
						reward = new ItemStack(Items.EXPERIENCE_BOTTLE, 1);
						rewardMessage = "§a达成30单词！获得附魔之瓶×1！";
						claimedMilestones.add(30);
					}
					break;
				case 40:
					if (!claimedMilestones.contains(40)) {
						reward = new ItemStack(Items.GOLD_INGOT, 3);
						rewardMessage = "§a达成40单词！获得金锭×3！";
						claimedMilestones.add(40);
					}
					break;
				case 50:
					if (!claimedMilestones.contains(50)) {
						reward = new ItemStack(Items.DIAMOND, 1);
						rewardMessage = "§a达成50单词！获得钻石×1！";
						claimedMilestones.add(50);
					}
					break;
				case 60:
					if (!claimedMilestones.contains(60)) {
						player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 600, 0));
						rewardMessage = "§a达成60单词！获得速度I（30秒）！";
						claimedMilestones.add(60);
					}
					break;
				case 70:
					if (!claimedMilestones.contains(70)) {
						reward = new ItemStack(Items.EMERALD, 2);
						rewardMessage = "§a达成70单词！获得绿宝石×2！";
						claimedMilestones.add(70);
					}
					break;
				case 80:
					if (!claimedMilestones.contains(80)) {
						player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 600, 0));
						rewardMessage = "§a达成80单词！获得力量I（30秒）！";
						claimedMilestones.add(80);
					}
					break;
				case 90:
					if (!claimedMilestones.contains(90)) {
						reward = new ItemStack(Items.NETHERITE_SCRAP, 2);
						rewardMessage = "§a达成90单词！获得下界合金碎片×2！";
						claimedMilestones.add(90);
					}
					break;
				case 100:
					if (!claimedMilestones.contains(100)) {
						reward = new ItemStack(Items.DIAMOND, 2);
						rewardMessage = "§a达成100单词！获得钻石×2！";
						claimedMilestones.add(100);
					}
					break;
				case 200:
					if (!claimedMilestones.contains(200)) {
						player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 0));
						rewardMessage = "§a达成200单词！获得力量I（60秒）！";
						claimedMilestones.add(200);
					}
					break;
				case 500:
					if (!claimedMilestones.contains(500)) {
						reward = new ItemStack(Items.NETHERITE_INGOT, 1);
						rewardMessage = "§a达成500单词！获得下界合金锭×1！";
						claimedMilestones.add(500);
					}
					break;
				case 1000:
					if (!claimedMilestones.contains(1000)) {
						reward = new ItemStack(Items.ELYTRA, 1);
						rewardMessage = "§a达成1000单词！获得鞘翅×1！";
						claimedMilestones.add(1000);
					}
					break;
				default:
					return;
			}
		} else {
			return;
		}

		if (rewardMessage != null) {
			if (reward != null) {
				player.giveItemStack(reward);
			}
			player.sendMessage(Text.literal(rewardMessage), true);
			PlayerDataManager.setRewardMilestones(playerId, claimedMilestones);
			LOGGER.info("Player {} received reward for {} correct words: {}", player.getName().getString(), correctCount, rewardMessage);
		}
	}

	private void registerNetworking() {
		ServerPlayNetworking.registerGlobalReceiver(WORD_ANSWER_PACKET, (server, player, handler, buf, responseSender) -> {
			int selectedIndex = buf.readInt();
			LOGGER.info("Server received answer from {}: Selected index {}", player.getName().getString(), selectedIndex);

			server.execute(() -> {
				AttackContext context = pendingAttacks.remove(player.getUuid());
				UUID playerId = player.getUuid();

				if (context != null) {
					if (context.target instanceof LivingEntity livingTarget) {
						livingTarget.removeStatusEffect(StatusEffects.SLOWNESS);
						livingTarget.removeStatusEffect(StatusEffects.BLINDNESS);
						LOGGER.info("Removed slowness and blindness from {}.", livingTarget.getName().getString());
					}

					boolean correct = selectedIndex == context.correctIndex;
					Word answeredWord = context.word;
					String wordListName = PlayerDataManager.getCurrentWordList(playerId);

					int currentConsecutiveCorrects = PlayerDataManager.getConsecutiveCorrects(playerId);
					int currentDamageStreak = PlayerDataManager.getDamageStreak(playerId);
					int currentConsecutiveWrongs = PlayerDataManager.getConsecutiveWrongs(playerId);

					if (correct) {
						currentConsecutiveCorrects++;
						currentDamageStreak++;
						PlayerDataManager.setConsecutiveCorrects(playerId, currentConsecutiveCorrects);
						PlayerDataManager.setDamageStreak(playerId, currentDamageStreak);
						PlayerDataManager.setConsecutiveWrongs(playerId, 0);

						LOGGER.info("Player {} answered correctly. Heal streak: {}, Damage streak: {}", player.getName().getString(), currentConsecutiveCorrects, currentDamageStreak);

						if (context.target instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
							float baseDamage = context.damage;
							float damageMultiplier = 1.0f;
							StringBuilder message = new StringBuilder();
							boolean healTriggered = false;

							if (currentDamageStreak >= 128) {
								damageMultiplier = 7.4f; // +640%
								message.append("§a128连击！伤害+640%");
							} else if (currentDamageStreak >= 64) {
								damageMultiplier = 4.2f; // +320%
								message.append("§a64连击！伤害+320%");
							} else if (currentDamageStreak >= 32) {
								damageMultiplier = 2.6f;
								message.append("§a32连击！伤害+160%");
							} else if (currentDamageStreak >= 16) {
								damageMultiplier = 1.8f;
								message.append("§a16连击！伤害+80%");
							} else if (currentDamageStreak >= 8) {
								damageMultiplier = 1.4f;
								message.append("§a8连击！伤害+40%");
							} else if (currentDamageStreak >= 4) {
								damageMultiplier = 1.2f;
								message.append("§a4连击！伤害+20%");
							}

							if (currentConsecutiveCorrects % 2 == 0) {
								player.heal(2.0f);
								healTriggered = true;
								if (message.length() > 0) {
									message.append("，回复2点生命值！");
								} else {
									message.append("§a连击达成！回复2点生命值！");
								}
								PlayerDataManager.setConsecutiveCorrects(playerId, 0);
								LOGGER.info("Player {} reached {} consecutive corrects. Healed 2 HP and reset heal streak.", player.getName().getString(), currentConsecutiveCorrects);
							}

							float finalDamage = baseDamage * damageMultiplier;
							livingTarget.damage(context.damageSource, finalDamage);

							int currentScore = playerCorrectCount.merge(playerId, 1, Integer::sum);
							player.sendMessage(Text.literal("§a正确！您已答对 §b" + currentScore + " §a个单词。"), false);

							if (message.length() > 0) {
								player.sendMessage(Text.literal(message.toString()), true);
								LOGGER.info("Player {} hit streak: {} (multiplier: {}, final damage: {}, heal: {})", player.getName().getString(), currentDamageStreak, damageMultiplier, finalDamage, healTriggered);
							}

							grantReward(player, currentScore);
							PlayerDataManager.removeWrongWord(playerId, answeredWord);

							LOGGER.info("Player {} answered correctly. Target {} damaged for {} HP.", player.getName().getString(), livingTarget.getName().getString(), finalDamage);
						} else {
							player.sendMessage(Text.literal("§e目标已消失或无效。"), true);
						}
					} else {
						PlayerDataManager.setConsecutiveWrongs(playerId, currentConsecutiveWrongs + 1);
						PlayerDataManager.setConsecutiveCorrects(playerId, 0);
						PlayerDataManager.setDamageStreak(playerId, 0);

						PlayerDataManager.addWrongWord(playerId, answeredWord);
						player.sendMessage(Text.literal("§c错误！攻击失败！"), true);

						int consecutiveWrongs = PlayerDataManager.getConsecutiveWrongs(playerId);
						if (consecutiveWrongs >= 2) {
							player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0));
							player.sendMessage(Text.literal("§c连续答错！受到减速惩罚。"), true);
							LOGGER.info("Player {} wrong streak: {} (applied slowness).", player.getName().getString(), consecutiveWrongs);
						}
					}
				} else {
					LOGGER.warn("No pending attack context for player {}", player.getName().getString());
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(CANCEL_CHALLENGE_PACKET, (server, player, handler, buf, responseSender) -> {
			server.execute(() -> {
				AttackContext context = pendingAttacks.remove(player.getUuid());
				UUID playerId = player.getUuid();

				PlayerDataManager.setConsecutiveCorrects(playerId, 0);
				PlayerDataManager.setDamageStreak(playerId, 0);
				PlayerDataManager.setConsecutiveWrongs(playerId, 0);
				LOGGER.info("Player {} cancelled challenge. Consecutive counts reset.", player.getName().getString());

				if (context != null) {
					if (context.target instanceof LivingEntity livingTarget) {
						livingTarget.removeStatusEffect(StatusEffects.SLOWNESS);
						livingTarget.removeStatusEffect(StatusEffects.BLINDNESS);
						LOGGER.info("Player {} cancelled challenge. Removed slowness and blindness from {}.", player.getName().getString(), livingTarget.getName().getString());
					}
					player.sendMessage(Text.literal("§e单词挑战已取消。"), true);
				} else {
					LOGGER.warn("Server: No pending attack context for player {} on cancel.", player.getName().getString());
				}
			});
		});
	}

	private void registerEvents() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer && entity instanceof LivingEntity) {
				showWordSelection(serverPlayer, entity);
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});
	}

	private void showWordSelection(ServerPlayerEntity player, Entity target) {
		UUID playerId = player.getUuid();
		String wordListName = PlayerDataManager.getCurrentWordList(playerId);
		List<Word> words = wordLists.getOrDefault(wordListName, wordLists.get("default"));
		if (words == null || words.isEmpty()) {
			player.sendMessage(Text.literal("§e单词库为空，无法进行背单词挑战！"), false);
			return;
		}

		Word correctWord;
		boolean isReviewWord = false;
		int currentCorrectCount = playerCorrectCount.getOrDefault(playerId, 0);
		int playerRepeatInterval = PlayerDataManager.getPlayerReviewInterval(playerId);
		List<Word> wrongWords = PlayerDataManager.getWrongWords(playerId);

		if (currentCorrectCount > 0 && currentCorrectCount % playerRepeatInterval == 0 && !wrongWords.isEmpty()) {
			correctWord = wrongWords.get(random.nextInt(wrongWords.size()));
			isReviewWord = true;
			LOGGER.info("Player {} (Correct: {}) is repeating a wrong word: {}", player.getName().getString(), currentCorrectCount, correctWord.getWord());
		} else {
			correctWord = words.get(random.nextInt(words.size()));
			LOGGER.info("Player {} (Correct: {}) is getting a new word: {}", player.getName().getString(), currentCorrectCount, correctWord.getWord());
		}

		String correctTranslation = correctWord.getPrimaryTranslation();
		List<String> optionsList = new ArrayList<>();
		optionsList.add(correctTranslation);

		List<Word> tempAvailableWords = new ArrayList<>(words);
		tempAvailableWords.remove(correctWord);
		Collections.shuffle(tempAvailableWords);

		for (int i = 0; i < 3; i++) {
			if (!tempAvailableWords.isEmpty()) {
				String randomTranslation = tempAvailableWords.remove(0).getPrimaryTranslation();
				if (!optionsList.contains(randomTranslation)) {
					optionsList.add(randomTranslation);
				} else {
					i--;
				}
			} else {
				optionsList.add("未知翻译" + (i + 1));
			}
		}
		while (optionsList.size() < 4) {
			optionsList.add("未知翻译" + optionsList.size());
		}

		Collections.shuffle(optionsList);
		String[] options = optionsList.toArray(new String[0]);

		int correctIndex = -1;
		for (int i = 0; i < options.length; i++) {
			if (options[i].equals(correctTranslation)) {
				correctIndex = i;
				break;
			}
		}

		if (target instanceof LivingEntity livingTarget) {
			livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 400, 4, false, false, true));
			LOGGER.info("Applied Slowness V to {} for 20 seconds.", livingTarget.getName().getString());
			livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0, false, false, true));
			LOGGER.info("Applied Blindness I to {} for 10 seconds.", livingTarget.getName().getString());
		}

		float attackDamage = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
		DamageSource damageSource = DamageSource.player(player);

		AttackContext context = new AttackContext(target, damageSource, attackDamage, correctIndex, correctWord);
		pendingAttacks.put(playerId, context);
		LOGGER.info("Server: Stored attack context for player {}. Target: {}, Damage: {}, Correct Index: {}, Word: {}", player.getName().getString(), target.getName().getString(), attackDamage, correctIndex, correctWord.getWord());

		net.minecraft.network.PacketByteBuf buf = PacketByteBufs.create();
		buf.writeString(correctWord.getWord());
		buf.writeString(correctWord.getAllTranslations());
		buf.writeInt(options.length);
		for (String option : options) {
			buf.writeString(option);
		}
		buf.writeInt(correctIndex);
		buf.writeBoolean(isReviewWord);
		ServerPlayNetworking.send(player, WORD_SELECTION_PACKET, buf);
		LOGGER.info("Server: Sent word selection packet to player {} for word '{}' from {} (Review: {}).", player.getName().getString(), correctWord.getWord(), wordListName, isReviewWord);
	}

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("englearn")
					.then(CommandManager.literal("set_review_interval")
							.then(CommandManager.argument("interval", IntegerArgumentType.integer(1))
									.executes(context -> {
										ServerPlayerEntity player = context.getSource().getPlayer();
										if (player == null) {
											context.getSource().sendError(Text.literal("此命令只能由玩家执行！"));
											return 0;
										}
										int interval = IntegerArgumentType.getInteger(context, "interval");
										PlayerDataManager.setPlayerReviewInterval(player.getUuid(), interval);
										player.sendMessage(Text.literal("§a复习间隔已设置为 §b" + interval + " §a个单词。"), false);
										LOGGER.info("Player {} set review interval to {}.", player.getName().getString(), interval);
										return 1;
									})))
					.then(CommandManager.literal("get_review_interval")
							.executes(context -> {
								ServerPlayerEntity player = context.getSource().getPlayer();
								if (player == null) {
									context.getSource().sendError(Text.literal("此命令只能由玩家执行！"));
									return 0;
								}
								int interval = PlayerDataManager.getPlayerReviewInterval(player.getUuid());
								player.sendMessage(Text.literal("§a您当前的复习间隔为 §b" + interval + " §a个单词。"), false);
								return 1;
							}))
					.then(CommandManager.literal("list_wrong_words")
							.executes(context -> {
								ServerPlayerEntity player = context.getSource().getPlayer();
								if (player == null) {
									context.getSource().sendError(Text.literal("此命令只能由玩家执行！"));
									return 0;
								}
								List<Word> wrongWords = PlayerDataManager.getWrongWords(player.getUuid());
								if (wrongWords.isEmpty()) {
									player.sendMessage(Text.literal("§a您目前没有错词，继续努力！"), false);
								} else {
									player.sendMessage(Text.literal("§e--- 我的错词本 ---"), false);
									Set<String> displayedWords = new HashSet<>();
									for (Word word : wrongWords) {
										if (displayedWords.add(word.getWord())) {
											String allTranslations = word.getAllTranslations().replace("(", "").replace(")", "");
											player.sendMessage(Text.literal("§b" + word.getWord() + "§r: " + allTranslations), false);
										}
									}
									player.sendMessage(Text.literal("§e-------------------"), false);
								}
								return 1;
							}))
					.then(CommandManager.literal("reset_streak")
							.executes(context -> {
								ServerPlayerEntity player = context.getSource().getPlayer();
								if (player == null) {
									context.getSource().sendError(Text.literal("此命令只能由玩家执行！"));
									return 0;
								}
								PlayerDataManager.setConsecutiveCorrects(player.getUuid(), 0);
								PlayerDataManager.setDamageStreak(player.getUuid(), 0);
								PlayerDataManager.setConsecutiveWrongs(player.getUuid(), 0);
								player.sendMessage(Text.literal("§a连击计数已重置。"), false);
								LOGGER.info("Player {} reset streak counts.", player.getName().getString());
								return 1;
							}))
					.then(CommandManager.literal("switch_wordlist")
							.then(CommandManager.argument("name", StringArgumentType.word())
									.suggests((ctx, builder) -> {
										availableWordLists.forEach(builder::suggest);
										return builder.buildFuture();
									})
									.executes(context -> {
										ServerPlayerEntity player = context.getSource().getPlayer();
										if (player == null) {
											context.getSource().sendError(Text.literal("此命令只能由玩家执行！"));
											return 0;
										}
										String wordListName = StringArgumentType.getString(context, "name");
										if (!availableWordLists.contains(wordListName)) {
											player.sendMessage(Text.literal("§c无效词库：§b" + wordListName + "§c。可用词库：§b" + String.join(", ", availableWordLists)), false);
											return 0;
										}
										PlayerDataManager.setCurrentWordList(player.getUuid(), wordListName);
										player.sendMessage(Text.literal("§a已切换到词库：§b" + wordListName), false);
										LOGGER.info("Player {} switched to word list {}.", player.getName().getString(), wordListName);
										return 1;
									}))
							.executes(context -> {
								ServerPlayerEntity player = context.getSource().getPlayer();
								if (player == null) {
									context.getSource().sendError(Text.literal("此命令只能由玩家执行！"));
									return 0;
								}
								String current = PlayerDataManager.getCurrentWordList(player.getUuid());
								player.sendMessage(Text.literal("§a当前词库：§b" + current + "§a。可用词库：§b" + String.join(", ", availableWordLists)), false);
								return 1;
							}))
					.then(CommandManager.literal("list_wordlists")
							.executes(context -> {
								ServerPlayerEntity player = context.getSource().getPlayer();
								if (player == null) {
									context.getSource().sendError(Text.literal("此命令只能由玩家执行！"));
									return 0;
								}
								player.sendMessage(Text.literal("§a可用词库：§b" + String.join(", ", availableWordLists)), false);
								return 1;
							})));
		});
	}

	public static class AttackContext {
		public final Entity target;
		public final DamageSource damageSource;
		public final float damage;
		public final int correctIndex;
		public final Word word;

		public AttackContext(Entity target, DamageSource damageSource, float damage, int correctIndex, Word word) {
			this.target = target;
			this.damageSource = damageSource;
			this.damage = damage;
			this.correctIndex = correctIndex;
			this.word = word;
		}
	}
}