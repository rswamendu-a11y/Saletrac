import re

with open('app/build.gradle.kts', 'r') as file:
    content = file.read()

# Insert packaging { jniLibs { useLegacyPackaging = true } } block into android block
packaging_block = '''
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
'''

content = content.replace('    signingConfigs {', packaging_block + '\n    signingConfigs {')

with open('app/build.gradle.kts', 'w') as file:
    file.write(content)
