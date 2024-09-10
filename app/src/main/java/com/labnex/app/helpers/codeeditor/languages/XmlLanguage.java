package com.labnex.app.helpers.codeeditor.languages;

import com.amrdeveloper.codeview.Code;
import com.amrdeveloper.codeview.Keyword;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author mmarif
 */
public class XmlLanguage extends Language {

	// Brackets and Colons
	private static final Pattern PATTERN_BUILTINS = Pattern.compile("[,:;[->]{}()]");

	// Data
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_CHAR = Pattern.compile("['](.*?)[']");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");
	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("<!--.*-->");
	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\.[a-zA-Z0-9_]+");
	private static final Pattern PATTERN_OPERATION =
			Pattern.compile(
					":|==|>|<|!=|>=|<=|->|=|>|<|%|-|-=|%=|\\+|\\-|\\-=|\\+=|\\^|\\&|\\|::|\\?|\\*");

	public static String getCommentStart() {
		return "<!--";
	}

	public static String getCommentEnd() {
		return "-->";
	}

	@Override
	public Pattern getPattern(LanguageElement element) {
		switch (element) {
			case KEYWORD:
				return Pattern.compile("\\b(" + String.join("|", getKeywords()) + ")\\b");
			case BUILTIN:
				return PATTERN_BUILTINS;
			case NUMBER:
				return PATTERN_NUMBERS;
			case CHAR:
				return PATTERN_CHAR;
			case STRING:
				return PATTERN_STRING;
			case HEX:
				return PATTERN_HEX;
			case SINGLE_LINE_COMMENT:
			case MULTI_LINE_COMMENT:
				return PATTERN_SINGLE_LINE_COMMENT;
			case ATTRIBUTE:
				return PATTERN_ATTRIBUTE;
			case OPERATION:
				return PATTERN_OPERATION;
			case GENERIC:
			case TODO_COMMENT:
			case ANNOTATION:
			default:
				return null;
		}
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"<xml", "encoding", "version",
		};
	}

	@Override
	public List<Code> getCodeList() {
		List<Code> codeList = new ArrayList<>();
		String[] keywords = getKeywords();
		for (String keyword : keywords) {
			codeList.add(new Keyword(keyword));
		}
		return codeList;
	}

	@Override
	public String getName() {
		return "XML";
	}

	@Override
	public Set<Character> getIndentationStarts() {
		Set<Character> characterSet = new HashSet<>();
		characterSet.add('{');
		return characterSet;
	}

	@Override
	public Set<Character> getIndentationEnds() {
		Set<Character> characterSet = new HashSet<>();
		characterSet.add('}');
		return characterSet;
	}
}
