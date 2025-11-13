# ANSI Log Viewer

Free JetBrains plugin adding ANSI color & formatting support to log-like files directly in the editor.

## Features
- ANSI SGR: standard + bright colors, styles (bold, italic, underline)
- 256-color and truecolor sequences (38;2;r;g;b / 48;2;r;g;b and 38;5;idx / 48;5;idx)
- Folding/hiding raw escape sequences
- Live re-highlight on edits (debounced)
- Configurable file extensions (default: log)

## Installation (Local Build)
1. Clone the repo
2. Run `./gradlew build`
3. Install the ZIP from `build/distributions` via Settings > Plugins > Install plugin from disk

## Marketplace Publishing
Set environment variable `IJ_TOKEN` with your JetBrains Marketplace token.
Run: `./gradlew runPluginVerifier` (compatibility check) then `./gradlew publishPlugin`.
Provide screenshots (PNG ~1200x800) showing: 1) colored sample-colored.log, 2) settings dialog.

## Configuration
Settings/Preferences > Tools > ANSI Log Viewer: edit comma-separated extensions (without dots).

## Limitations / Notes
- Very large files (>5MB) skip auto re-highlight on edit for performance.
- Does not strip escape sequences from disk; only folds them visually.
- Truecolor applies directly; theme contrast may vary.

## Changelog
### 0.1.1
- Settings panel, live update, 256-color & truecolor.
### 0.1.0
- Initial release (basic colors + folding).

## License
MIT
