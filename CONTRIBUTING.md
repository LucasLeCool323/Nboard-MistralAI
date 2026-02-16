# Contributing to Nboard

Thank you for your interest in contributing to Nboard!

## Bug Reports

Bug reports are always welcome. Please include:
- Clear description of the bug
- Steps to reproduce
- Expected vs actual behavior
- Device information (device model, Android version, Nboard version)
- Screenshots or logs if applicable

Open an issue using the "Bug Report" template.

## Bug Fix Pull Requests

Bug fix PRs are welcome and will likely be merged if:
- The fix resolves the issue described
- It doesn't break existing functionality
- Code follows the existing style
- You've tested it on your device

## Feature Contributions

Before starting work on a new feature, please note that Nboard has a focused scope. See the README section "What Won't Be Implemented" for features that won't be accepted.

For features not on that list, it's best to open an issue first to discuss whether it aligns with the project direction before investing time in implementation.

## What Won't Be Accepted

The following feature PRs will be closed as they won't be implemented (FORK instead):

- Keyboard size or height customization
- One-handed mode
- Custom themes beyond built-in options
- Floating keyboard mode
- Advanced customization options (key borders, corner radius, custom colors, etc.)
- Cloud features (sync, backup, import/export)

Those features could be implemented but are not planned:

- Media features (GIF search, stickers)
- Password manager integration

You can open PRs for these features:

- Additional keyboard layouts beyond the built-ins

### Want These Features?

Nboard is open source (AGPL-3.0). You're welcome to fork the repository and add any features you'd like for your own use. The v1.3.0 refactor made the codebase modular and easier to extend.

If you create a compelling fork with features others want, you can maintain and distribute it yourself.

## Custom Layout XML (No Fork Needed)

You can create and share custom keyboard layouts as XML files and import them directly in app settings.

Use [`layout_template.xml`](layout_template.xml) as your starting point.

### How to create a layout pack

1. Copy `layout_template.xml` and rename it (for example `my_language.xml`)
2. Set a unique `id` (do not use built-in IDs like `builtin.*`)
3. Set a `name` shown in the layout picker
4. Choose `bottomStyle`:
   - `classic` for legacy bottom row behavior
   - `gboard` for Gboard-style bottom row behavior
5. Fill `row1`, `row2`, `row3` with space-separated keys
6. (Optional) Define custom long-press variants:
   - `<variants><key value="a">à á â</key></variants>`
   - Use `value` (or `base`) to target the key
   - Separate options with spaces
7. Keep each row between 5 and 12 keys, each token up to 4 characters
8. Keep variant base keys and variant tokens up to 4 characters
9. Save as UTF-8 `.xml`
10. Import from app settings: `Layout packs` -> `Import layout pack`

### Share your layout in this repo

Don't hesitate to open a PR adding your layout XML to [`Community Layouts/`](Community%20Layouts/).

- Use lowercase kebab-case filenames
- Preferred format: `<language>-<layout>-<style>.xml`
- Example: `french-azerty-gboard.xml`

### What layout XML changes

- Key positions and displayed characters
- Whether the layout behaves as qwerty-like (`qwertyLike`)
- Bottom row style (`classic` or `gboard`)
- Long-press variants per key (`variants`)

### What layout XML does not change

- Autocorrect dictionaries
- Word prediction dictionaries and n-grams
- Voice model/language packs

Those parts are still limited to the core app behavior (French/English in upstream Nboard).

## Full Language Support

If you want a full language implementation (layout + autocorrect + prediction + voice), you'll need to maintain your own version of Nboard.

## Code Style

- Follow existing Kotlin conventions in the codebase
- Keep functions focused and reasonably sized
- Use descriptive variable names
- Add comments for complex logic
- Organize new code into appropriate modules following the v1.3.0 architecture

## Questions Before Starting

If you're planning to work on a significant contribution:
1. Check the "What Won't Be Implemented" section in the README
2. If your feature is on that list, consider forking instead
3. If you're unsure, open an issue to discuss before starting work

## Review Process

- Bug fixes: Usually reviewed within a few days
- Layout pack and documentation updates: Usually reviewed quickly
- Other features: Will be evaluated based on project scope alignment

Thank you for contributing to Nboard.
