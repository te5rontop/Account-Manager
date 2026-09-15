# Changelog

All notable changes to Account Manager are documented in this file.

## [1.0.1] - 2026-09-02

Security and reliability maintenance release for Account Manager.

### Security

- Centralized Microsoft and Minecraft access-token state in a thread-safe in-memory session store.
- Added session generations so results from stale asynchronous authentication operations are discarded after account clearing or a new sign-in.
- Clear Account now invalidates previous authentication generations to reduce the risk of stale session state being restored.
- Minecraft profile, skin-upload and name-change requests now verify that they still belong to the active authentication session.
- Added connection and request timeouts to Xbox, XSTS and Minecraft Services HTTP requests.
- Added a 2 MB maximum skin-file size check before a skin is uploaded.
- Updated Microsoft Authentication Library for Java (MSAL4J) from 1.19.1 to 1.26.0.
- Pinned third-party GitHub Actions to exact commit SHAs for stronger CI supply-chain integrity.
- Added the official SHA-256 checksum for the Gradle 9.5.1 binary distribution to the Gradle wrapper configuration.

### Changed

- Internal authentication-session handling was refactored without intentionally changing the public Account Manager workflow.
- Project version updated to 1.0.1.

### Minecraft Services Authorization

- **Status updated 2026-09-15:** Account Manager's Microsoft Entra application has been approved for Minecraft Services access.
- The Microsoft OAuth -> Xbox Live -> XSTS -> Minecraft Services flow was re-tested successfully with v1.0.1 after authorization was granted.
- Minecraft profile loading, IGN / UUID retrieval, skin uploading and Minecraft profile-name changes were verified working successfully.

## [1.0.0] - 2026-09-02

Initial public release of Account Manager for Minecraft: Java Edition 26.1.2 using Fabric.

### Added

- Microsoft sign-in through the user's system browser.
- Account Manager entry point integrated into the Multiplayer screen.
- Microsoft and Minecraft authentication-status display.
- Minecraft profile viewer with IGN, UUID and current skin preview.
- Manual Minecraft profile refresh.
- Dedicated Profile Management interface.
- Dedicated Skin Management interface.
- Dedicated IGN Editor interface with local name validation.
- Native PNG skin selection with 64x64 validation.
- Classic and Slim player-model skin previews.
- Minecraft skin upload integration for authorized Minecraft Services sessions.
- Minecraft IGN change integration for authorized Minecraft Services sessions.
- In-memory authentication state with no intentional token persistence to disk.
- Clear Account action for removing the mod's in-memory authentication state.
- Custom Account Manager UI, project branding and moving Discord watermark.

### Tested

- Verified in a clean Minecraft: Java Edition 26.1.2 Fabric installation.
- Core Account Manager, Profile Management and Skin Management interfaces verified working.
- Skin selection and Classic/Slim preview flow verified working.
- Microsoft browser-based sign-in flow verified up to Minecraft Services application authorization.

### Requirements

- Minecraft: Java Edition 26.1.2
- Fabric Loader 0.19.3+
- Fabric API compatible with Minecraft 26.1.2
- Java 25+

### Historical Limitation at Release

- At the time v1.0.0 was published, the application's Microsoft Entra Client ID was still awaiting Minecraft Services allow-list approval.
- This limitation was later resolved when Minecraft Services access was approved and successfully re-tested with v1.0.1 on 2026-09-15.
- Account Manager uses its own Microsoft Entra public-client registration and does not use another application's Client ID or attempt to bypass Minecraft Services authorization.

### Security

- Authentication is performed through Microsoft's browser-based OAuth flow.
- No client secret is embedded in the mod.
- Access tokens and related authentication state are intended to remain in memory while Minecraft is running.
- Users should never share Microsoft passwords, access tokens, refresh tokens, Xbox/XSTS tokens or authorization codes when requesting support.

[1.0.1]: https://github.com/te5rontop/Account-Manager/releases/tag/v1.0.1
[1.0.0]: https://github.com/te5rontop/Account-Manager/releases/tag/v1.0.0
