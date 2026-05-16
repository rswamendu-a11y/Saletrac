with open('app/build.gradle.kts', 'r') as file:
    content = file.read()

content = content.replace('namespace = "com.saletrac"', 'namespace = "com.exclusive.saletrac"')
content = content.replace('applicationId = "com.saletrac"', 'applicationId = "com.exclusive.saletrac"')

signing_old = '''    signingConfigs {
        getByName("debug") {
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }'''

signing_new = '''    signingConfigs {
        getByName("debug") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }'''

content = content.replace(signing_old, signing_new)

build_type_old = '''    buildTypes {
        getByName("debug") {
            isZipAlignEnabled = true
            signingConfig = signingConfigs.getByName("debug")
        }'''

build_type_new = '''    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isZipAlignEnabled = true
            signingConfig = signingConfigs.getByName("debug")
        }'''

content = content.replace(build_type_old, build_type_new)

with open('app/build.gradle.kts', 'w') as file:
    file.write(content)
