# English Learning Mod

**English Learning Mod** 是一款为 **Minecraft 1.19.2（Fabric）** 设计的教育娱乐模组，将英语单词学习融入游戏冒险！通过攻击生物触发单词挑战，玩家可以在战斗中提升词汇量。答对可获得伤害加成、生命恢复和丰厚奖励，答错则记录错词以供复习。适合喜欢Minecraft的英语学习者，寓教于乐！

---

## 模组信息

- **名称**: English Learning Mod
- **模组ID**: `englearning`
- **版本**: 1.0.0
- **适用Minecraft版本**: 1.19.2
- **依赖**:
  - Fabric Loader >=0.14.21
  - Fabric API 0.76.0+1.19.2
  - Java >=17
- **许可证**: MIT
- **开发者**: [40441w]

---

## 核心功能

### 1. 单词挑战系统

- **触发**: 攻击活体生物（如僵尸、牛）时，暂停攻击，弹出单词选择界面。
- **内容**: 显示英语单词（默认考研词汇），提供4个中文翻译选项，选择正确翻译。
- **结果**:
  - **正确**: 攻击生效，可能触发伤害加成、生命恢复或奖励。
  - **错误**: 攻击失败，单词加入错词本，连续2次错误触发减速I（3秒）。
- **取消**: 可取消挑战，清除连击并移除目标的缓慢和失明效果。

### 2. 连击与伤害加成

- **机制**: 连续答对（`damageStreak`）增加伤害倍率，答错或取消重置。
- **加成**:
  - 4连击: +20%（1.2倍）
  - 8连击: +40%（1.4倍）
  - 16连击: +80%（1.8倍）
  - 32连击: +160%（2.6倍）
  - 64连击: +320%（4.2倍）
  - 128连击: +640%（7.4倍）
- **播报**: 动作栏显示，如“§a64连击！伤害+320%！”。

### 3. 生命恢复

- **机制**: 每2次答对（`consecutiveCorrects`）恢复2点生命值（1颗心），重置连击。
- **播报**: 显示“§a连击达成！回复2点生命值！”或与增伤合并（如“§a4连击！伤害+20%，回复2点生命值！”）。
- **限制**: 满血时播报触发，生命不超上限。

### 4. 错词复习

- **错词本**: 答错单词记录到玩家数据。
- **复习**: 默认每15个正确答案（可调整），随机抽取错词挑战。
- **管理**:
  - 答对错词时移除。
  - 查看错词：`/englearn list_wrong_words`。

### 5. 奖励系统

- **机制**: 累计答对单词数（`playerCorrectCount`）达里程碑时发放奖励。
- **奖励表格**:

| 单词数 | 奖励内容       |
| ------ | -------------- |
| 10     | 经验等级+1     |
| 20     | 铁锭×5         |
| 30     | 附魔之瓶×1     |
| 40     | 金锭×3         |
| 50     | 钻石×1         |
| 60     | 速度I（30秒）  |
| 70     | 绿宝石×2       |
| 80     | 力量I（30秒）  |
| 90     | 下界合金碎片×2 |
| 100    | 钻石×2         |
| 200    | 力量I（60秒）  |
| 500    | 下界合金锭×1   |
| 1000   | 鞘翅×1         |

- **播报**: 如“§a达成10单词！获得经验等级+1！”。

### 6. 命令

- `/englearn set_review_interval <interval>`: 设置错词复习间隔。
- `/englearn get_review_interval`: 查看复习间隔。
- `/englearn list_wrong_words`: 列出错词本。
- `/englearn reset_streak`: 重置连击计数。

### 7. 目标效果

- 挑战期间，目标生物获得缓慢V（20秒）和失明I（10秒）。
- 挑战结束（答题或取消）移除效果。

### 8. 数据持久化

- **保存内容**: 错词本、复习间隔、连击计数、奖励里程碑。
- **方式**: 存储在世界数据（`englearning_player_<UUID>`）。
- **自动保存**: 服务器关闭或数据变更时触发。

---

## 安装方法

1. **准备环境**:
   - 安装 Minecraft 1.19.2（Fabric）。
   - 下载 [Fabric Loader](https://fabricmc.net/)（>=0.14.21）。
   - 下载 [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)（0.76.0+1.19.2）。

2. **安装模组**:
   - 下载 `englearning-1.0.0.jar`。
   - 放入 `~/.minecraft/mods`（或 `%appdata%\.minecraft\mods`）。
   - 启动游戏，选择 Fabric 1.19.2 配置文件。

3. **验证**:
   - 主菜单“Mods”显示“English Learning Mod”。
   - 攻击生物，弹出单词界面。

---

## 使用指南

1. **开始学习**:
   - 攻击活体生物，弹出单词选择界面。
   - 选择正确翻译，查看“§a正确！您已答对 §bX §a个单词。”。

2. **连击策略**:
   - 连续答对提升伤害，适合清怪。
   - 每2次答对恢复生命，适合低血量。
   - 谨慎答题，避免攻击失败。

3. **管理错词**:
   - 用 `/englearn list_wrong_words` 查看错词。
   - 调整复习：`/englearn set_review_interval 10`。

4. **追求奖励**:
   - 累计答对，解锁奖励（如鞘翅）。
   - 跟踪进度：观察答对次数。

5. **重置**:
   - `/englearn reset_streak` 重置连击。

---

## 技术细节

- **代码结构**:
  - `Englearning.java`: 主类，处理初始化、联网、事件、命令。
  - `PlayerData.java`: 玩家数据（错词、连击、奖励），支持NBT持久化。
  - `PlayerDataManager.java`: 管理玩家数据，加载/保存。
  - `Word.java`: 单词数据结构，解析词汇文件。
- **数据文件**:
  - `kaoyan.json`: 考研词汇，位于 `assets/englearning/data/`。
  - 格式：`[{"word": "abandon", "trans": [{"ch": "放弃", "type": "v"}], "sentence": []}, ...]`
- **联网**:
  - `WORD_SELECTION_PACKET`: 发送单词和选项。
  - `WORD_ANSWER_PACKET`: 接收答案，处理攻击和奖励。
  - `CANCEL_CHALLENGE_PACKET`: 取消挑战。
- **持久化**:
  - 使用 `PersistentState`，存储在世界文件夹。
  - 自动保存于服务器关闭或数据变更。

---

## 已知问题与解决

1. **增伤未触发**:
   - **原因**: 连击计数未保存。
   - **解决**: 更新至最新代码，确保 `PlayerDataManager` 持久化 `damageStreak`。
   - **验证**: 日志含 `Debug: Damage streak = X`。

2. **播报冲突**:
   - **原因**: 动作栏消息覆盖。
   - **解决**: 合并播报，单条消息显示。
   - **验证**: 答对4次，显示“§a4连击！伤害+20%，回复2点生命值！”。

3. **数据丢失**:
   - **原因**: 保存逻辑错误。
   - **解决**: 优化 `saveAllPlayerData`。
   - **验证**: 重启后 `damageStreak` 保留。

4. **单词未加载**:
   - **原因**: `kaoyan.json` 路径错误。
   - **解决**: 确认文件路径，检查日志 `Loaded X words`。

---

## 常见问题

**Q: 可以修改奖励吗？**

- **A**: 编辑 `Englearning.java` 的 `grantReward`，调整里程碑，重新构建。


## 致谢

感谢 Fabric 社区、Minecraft modding 社区及所有测试者！希望您在冒险中享受英语学习的乐趣！

**开始您的单词冒险吧！**

---

# English Version

## English Learning Mod

**English Learning Mod** is an educational entertainment mod for **Minecraft 1.19.2 (Fabric)**, blending English vocabulary learning with gameplay adventure! By attacking mobs to trigger word challenges, players can expand their vocabulary while fighting. Correct answers grant damage boosts, health recovery, and generous rewards, while incorrect answers log words for review. Perfect for Minecraft fans learning English, combining education with fun!

---

### Mod Information

- **Name**: English Learning Mod
- **Mod ID**: `englearning`
- **Version**: 1.0.0
- **Supported Minecraft Version**: 1.19.2
- **Dependencies**:
  - Fabric Loader >=0.14.21
  - Fabric API 0.76.0+1.19.2
  - Java >=17
- **License**: MIT
- **Developer**: [40441w]

---

### Core Features

#### 1. Word Challenge System

- **Trigger**: Attacking a living mob (e.g., zombie, cow) pauses the attack and displays a word selection interface.
- **Content**: Shows an English word (default: postgraduate exam vocabulary) with 4 Chinese translation options; select the correct one.
- **Outcome**:
  - **Correct**: Attack proceeds, potentially triggering damage boosts, health recovery, or rewards.
  - **Incorrect**: Attack fails, word is added to the wrong words list, and 2 consecutive errors apply Slowness I (3 seconds).
- **Cancel**: Cancel the challenge to clear combos and remove Slowness and Blindness from the target.

#### 2. Combo and Damage Boost

- **Mechanism**: Consecutive correct answers (`damageStreak`) increase damage multiplier; incorrect answers or cancellation reset it.
- **Boosts**:
  - 4 Combo: +20% (1.2x)
  - 8 Combo: +40% (1.4x)
  - 16 Combo: +80% (1.8x)
  - 32 Combo: +160% (2.6x)
  - 64 Combo: +320% (4.2x)
  - 128 Combo: +640% (7.4x)
- **Notification**: Action bar displays, e.g., “§a64 Combo! Damage +320%!”.

#### 3. Health Recovery

- **Mechanism**: Every 2 correct answers (`consecutiveCorrects`) restore 2 health points (1 heart), resetting the combo.
- **Notification**: Shows “§aCombo Achieved! Restored 2 Health!” or combined with boost, e.g., “§a4 Combo! Damage +20%, Restored 2 Health!”.
- **Limit**: Triggers notification at full health, but health doesn’t exceed maximum.

#### 4. Wrong Word Review

- **Wrong Word List**: Incorrectly answered words are recorded in player data.
- **Review**: By default, every 15 correct answers (adjustable), a wrong word is randomly selected for challenge.
- **Management**:
  - Correctly answering a wrong word removes it.
  - View wrong words: `/englearn list_wrong_words`.

#### 5. Reward System

- **Mechanism**: Cumulative correct words (`playerCorrectCount`) reaching milestones grant rewards.
- **Reward Table**:

| Word Count | Reward Content          |
| ---------- | ----------------------- |
| 10         | Experience Level +1     |
| 20         | Iron Ingots ×5          |
| 30         | Experience Bottle ×1    |
| 40         | Gold Ingots ×3          |
| 50         | Diamond ×1              |
| 60         | Speed I (30 seconds)    |
| 70         | Emeralds ×2             |
| 80         | Strength I (30 seconds) |
| 90         | Netherite Scraps ×2     |
| 100        | Diamonds ×2             |
| 200        | Strength I (60 seconds) |
| 500        | Netherite Ingot ×1      |
| 1000       | Elytra ×1               |

- **Notification**: E.g., “§aReached 10 Words! Gained Experience Level +1!”.

#### 6. Commands

- `/englearn set_review_interval <interval>`: Set wrong word review interval.
- `/englearn get_review_interval`: View review interval.
- `/englearn list_wrong_words`: List wrong words.
- `/englearn reset_streak`: Reset combo counters.

#### 7. Target Effects

- During challenges, target mobs gain Slowness V (20 seconds) and Blindness I (10 seconds).
- Effects are removed upon challenge completion (answering or cancellation).

#### 8. Data Persistence

- **Saved Data**: Wrong word list, review interval, combo counters, reward milestones.
- **Method**: Stored in world data (`englearning_player_<UUID>`).
- **Auto-Save**: Triggered on server shutdown or data changes.

---

### Installation

1. **Prepare Environment**:
   - Install Minecraft 1.19.2 (Fabric).
   - Download [Fabric Loader](https://fabricmc.net/) (>=0.14.21).
   - Download [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) (0.76.0+1.19.2).

2. **Install Mod**:
   - Download `englearning-1.0.0.jar`.
   - Place in `~/.minecraft/mods` (or `%appdata%\.minecraft\mods`).
   - Launch game with Fabric 1.19.2 profile.

3. **Verify**:
   - “Mods” menu shows “English Learning Mod”.
   - Attack a mob to trigger the word interface.

---

### Usage Guide

1. **Start Learning**:
   - Attack a living mob to trigger the word selection interface.
   - Select the correct translation, see “§aCorrect! You’ve answered §bX §acorrect words.”.

2. **Combo Strategy**:
   - Consecutive correct answers boost damage, ideal for clearing mobs.
   - Every 2 correct answers restore health, useful when low.
   - Answer carefully to avoid attack failure.

3. **Manage Wrong Words**:
   - Use `/englearn list_wrong_words` to view wrong words.
   - Adjust review: `/englearn set_review_interval 10`.

4. **Pursue Rewards**:
   - Accumulate correct answers to unlock rewards (e.g., Elytra).
   - Track progress via correct word count.

5. **Reset**:
   - `/englearn reset_streak` to reset combos.

---

### Technical Details

- **Code Structure**:
  - `Englearning.java`: Main class, handles initialization, networking, events, commands.
  - `PlayerData.java`: Player data (wrong words, combos, rewards), supports NBT persistence.
  - `PlayerDataManager.java`: Manages player data, loading/saving.
  - `Word.java`: Word data structure, parses vocabulary files.
- **Data Files**:
  - `kaoyan.json`: Postgraduate exam vocabulary, located in `assets/englearning/data/`.
  - Format: `[{"word": "abandon", "trans": [{"ch": "放弃", "type": "v"}], "sentence": []}, ...]`
- **Networking**:
  - `WORD_SELECTION_PACKET`: Sends word and options.
  - `WORD_ANSWER_PACKET`: Receives answers, processes attacks and rewards.
  - `CANCEL_CHALLENGE_PACKET`: Cancels challenges.
- **Persistence**:
  - Uses `PersistentState`, stored in world folder.
  - Auto-saves on server shutdown or data changes.

---

### Known Issues and Solutions

1. **Damage Boost Not Triggering**:
   - **Cause**: Combo counter not saved.
   - **Solution**: Update to latest code, ensure `PlayerDataManager` persists `damageStreak`.
   - **Verify**: Logs show `Debug: Damage streak = X`.

2. **Notification Conflicts**:
   - **Cause**: Action bar messages overlap.
   - **Solution**: Merge notifications into single message.
   - **Verify**: Answer 4 times, see “§a4 Combo! Damage +20%, Restored 2 Health!”.

3. **Data Loss**:
   - **Cause**: Save logic error.
   - **Solution**: Optimize `saveAllPlayerData`.
   - **Verify**: `damageStreak` persists after restart.

4. **Words Not Loading**:
   - **Cause**: Incorrect `kaoyan.json` path.
   - **Solution**: Verify file path, check logs for `Loaded X words`.

---

### FAQ

**Q: Can I modify rewards?**

- **A**: Edit `grantReward` in `Englearning.java`, adjust milestones, and rebuild.

---


### Acknowledgments

Thanks to the Fabric community, Minecraft modding community, and all testers! Enjoy learning English during your adventures!

**Start your word adventure now!**
