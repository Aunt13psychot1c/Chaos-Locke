<p align="center">
  <img src="docs/icon.png" width="160" alt="Chaos Locke icon: a black chaos star with a glowing blue padlock in its center ring">
</p>

# 🔐 Chaos Locke

**Secret messages in tough-guy emoji, plus imageboard tripcodes.** Lock a message with a key and it turns into something like 🔐🐗🦏🚂🐉🔥💀… Only someone with the same key can turn it back into words.

Made by **Aunt13psychot1c**, the Queen of Chaos. Sister app to [Chaos Bell](https://github.com/Aunt13psychot1c/Chaos-Bell) 🔔 and [Chaos Hub](https://github.com/Aunt13psychot1c/Chaos-Hub) 📱

## ✨ What it does

- 🔒 **Lock and unlock messages** with a secret key. They come out as 64 kinds of emoji (no hearts, no fruit).
- 📤 **Send it anywhere:** Messenger, Discord, texts. Or share a locked message *into* Chaos Locke from another app, or highlight it and pick Chaos Locke from the pop-up menu.
- 🎭 **Tripcodes:** type `Name#key` and see your imageboard tripcode, with every step of the recipe shown.
- 📖 **Chaos Locke for Dummies**, a built-in plain-English guide that opens the first time.
- 🛡️ **No internet permission at all.** Nothing you type leaves your phone.

## 📥 Install

1. Open the [latest release](https://github.com/Aunt13psychot1c/Chaos-Locke/releases/latest) on your phone and tap **Chaos-Locke-x.x.x.apk**.
2. Open the download. If your phone asks, let your browser **install unknown apps**.
3. If Play Protect warns you, tap **More details**, then **Install anyway**. Chaos Locke isn't on the Play Store yet, so Google just doesn't know it.

## 🛡️ How strong is it?

Diary-lock strong. It's great for private notes between friends, but the lock is DES, from the 1970s, so a serious expert with big computers could break it. Don't lock bank details, passwords, or anything dangerous with it.

## 🛠️ For builders

Kotlin + Jetpack Compose. The cipher is the classic Unix DES `crypt(3)`: the tripcode recipe picks a salt from the key, the salt twists DES's expansion table, and messages run through that twisted DES in CBC mode with a random first block, then get written out 6 bits per emoji. It's byte-for-byte compatible with the Tripcode Forge web page. Tests check it against the FIPS DES example, the system's own `crypt(3)`, and messages locked on the web page.

Push any branch and **Build My APK** makes a test copy that installs next to the real app. **Release Chaos Locke** builds the signed release, with the version taken from `VERSION`.

MIT licensed. See [LICENSE](LICENSE).
