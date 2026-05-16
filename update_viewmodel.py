import re

with open('app/src/main/java/com/saletrac/ui/SalesEntryViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('import kotlinx.coroutines.flow.combine', 'import kotlinx.coroutines.flow.map')
content = re.sub(
    r'val isSaveEnabled: StateFlow<Boolean> = combine\(\s*_uiState\s*\) \{ \(state\) ->',
    'val isSaveEnabled: StateFlow<Boolean> = _uiState.map { state ->',
    content
)

with open('app/src/main/java/com/saletrac/ui/SalesEntryViewModel.kt', 'w') as f:
    f.write(content)
