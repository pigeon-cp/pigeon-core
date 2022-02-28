# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

## 0.2 (2022-02-28)

### Features

* defines interfaces about plugin docs & add simple docs desc for MessageTemplateFactory#Default ([97aa27c](https://github.com/pigeon-cp/pigeon-core/commit/97aa27c48b1d6d978c93a442aa8e1ac5099fb83c))
* extend domain model for support mass messaging ([58cae17](https://github.com/pigeon-cp/pigeon-core/commit/58cae17c493ffd68d3e46f69bfbbfcc207c14b37))
* provide @Extensible annotation for display dynamic docs of those field extended by plugin ([0ad2955](https://github.com/pigeon-cp/pigeon-core/commit/0ad295549f8cb327148614d4c1de9433158b04da))
* provide actuator endpoint ([2c88c3a](https://github.com/pigeon-cp/pigeon-core/commit/2c88c3a4489b2fd3aabbf2dec4b88d8b74a05fc5))
* provide all extensions' info by endpoint 'actuator/pigeon/info' ([3867acf](https://github.com/pigeon-cp/pigeon-core/commit/3867acf2a37facc86d9d39009c49bae5034cf233))
* provide built-in exceptions for factory ([9adfe69](https://github.com/pigeon-cp/pigeon-core/commit/9adfe69b45cc02f4d0fd79715502e59e70c919a4))
* provide common PigeonPlugin base class for plugin project ([ea4e364](https://github.com/pigeon-cp/pigeon-core/commit/ea4e36425374fb83b0a6db7f3c9f5a8fc1ba554b))
* provide ConsoleMessage & LogMessage ([5dbac73](https://github.com/pigeon-cp/pigeon-core/commit/5dbac730eabfaf805ec67989d6f7b868fc3e6959))
* provide DummyMessage ([255f705](https://github.com/pigeon-cp/pigeon-core/commit/255f705136617552bac1e132f28e282919cdf3ba))
* provide ErrorCodeFinder for find all error code defined on runtime ([0c979ed](https://github.com/pigeon-cp/pigeon-core/commit/0c979eda85f862c167699932fa3d9f0838a8e260))
* provide Mail#Default(send mail via standard pop3/smtp/imap protocol) by default ([fac3f42](https://github.com/pigeon-cp/pigeon-core/commit/fac3f429fcaa33165bca5332a603774fafe091fd))
* provide PigeonContext for access main context's repo bean in plugin context ([8746c8f](https://github.com/pigeon-cp/pigeon-core/commit/8746c8f3b5b5b9e5d8398956c91e1998abea1b2e))
* publish event after tactic prepare ([e62b273](https://github.com/pigeon-cp/pigeon-core/commit/e62b273b96d36a3ad0c0d430c93d405a2fd282fe))
* support resolve json obj string ([c133cb5](https://github.com/pigeon-cp/pigeon-core/commit/c133cb55204adf4d80e151c354fd2fb349bad5f1))
* support specify placeholder rule for template ([dd5816f](https://github.com/pigeon-cp/pigeon-core/commit/dd5816fbce839366d44e35b3b4a2755160ba3237))

## 0.1 (2022-01-24)

### Features

* support mass messaging ([58cae17](https://github.com/pigeon-cp/pigeon-core/commit/58cae17c493ffd68d3e46f69bfbbfcc207c14b37))
* provide concept 'User' in domain model ([b270f87](https://github.com/pigeon-cp/pigeon-core/commit/b270f8715cd2eb204b5efa681d2ee41c57195bf0))
* provide concepts about sms for domain model ([a5a5f69](https://github.com/pigeon-cp/pigeon-core/commit/a5a5f6949d5291a5e28534aa68ad41b8ed24ba65))
* support message template ([b04f8b0](https://github.com/pigeon-cp/pigeon-core/commit/b04f8b043c30add3b03050887be2a9b2528cab7b))
* support plugin capable by introduce pf4j ([721eed1](https://github.com/pigeon-cp/pigeon-core/commit/721eed11ad7eb1285caa7c5d17f0a92f556a81ac))
