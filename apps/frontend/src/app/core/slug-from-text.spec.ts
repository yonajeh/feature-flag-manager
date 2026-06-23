import { slugFromText } from './slug-from-text';

describe('slugFromText', () => {
  it('converts display text to lowercase kebab-case', () => {
    expect(slugFromText('My Cool Feature')).toBe('my-cool-feature');
  });

  it('strips leading non-letters', () => {
    expect(slugFromText('123 My App')).toBe('my-app');
  });

  it('returns empty string for non-sluggable input', () => {
    expect(slugFromText('   ')).toBe('');
    expect(slugFromText('123')).toBe('');
  });
});
