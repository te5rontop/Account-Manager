# Security Policy

## Supported Versions

Security fixes are provided for the latest published release of Account Manager.

| Version | Supported |
| --- | --- |
| 1.0.x | Yes |
| Older versions | No |

## Reporting a Vulnerability

Please do **not** open a public GitHub issue for vulnerabilities involving authentication, tokens, account access, OAuth, Xbox/XSTS authentication, Minecraft Services, or other sensitive behavior.

If GitHub's private vulnerability reporting option is available for this repository, use that method. Otherwise, contact the project author through the support/contact information listed in the repository README.

When reporting a vulnerability, include:

- A clear description of the issue
- The affected Account Manager version
- Minecraft, Fabric Loader, Fabric API, and Java versions
- Steps to reproduce the issue
- The expected and actual behavior
- Relevant logs or screenshots with sensitive values removed
- Any suggested mitigation, if known

## Sensitive Information

Never include any of the following in a report, screenshot, log, issue, or public message:

- Microsoft account passwords
- OAuth authorization codes
- Access tokens
- Refresh tokens
- Xbox Live tokens
- XSTS tokens
- Minecraft Services bearer tokens
- Session credentials or cookies
- Client secrets or private keys

If a credential is accidentally exposed, revoke or rotate it before continuing with the report.

## Scope

Security reports are especially useful for issues involving:

- Authentication or account-access flaws
- Accidental token logging or persistence
- Exposure of sensitive account information
- Unauthorized profile, skin, or name-management behavior
- Insecure handling of Microsoft OAuth or Minecraft Services responses
- Vulnerabilities caused by Account Manager's own code or bundled dependencies

Issues that only affect unsupported Minecraft versions, modified builds, unofficial forks, or third-party software outside Account Manager may be out of scope.

## Responsible Disclosure

Please allow reasonable time for investigation and a fix before publishing technical details about a confirmed vulnerability.

Account Manager is an independent community project and is not affiliated with Mojang Studios, Microsoft, Xbox, or Fabric.
