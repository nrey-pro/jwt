package eu.webtoolkit.jwt;

import java.io.IOException;
import java.util.ArrayList;

import eu.webtoolkit.jwt.utils.StringUtils;

class EscapeOStream {
	public enum RuleSet {
		HtmlAttribute, JsStringLiteralSQuote, JsStringLiteralDQuote
	};

	public EscapeOStream(Appendable sink) {
		sink_ = sink;
		mixed_ = null;
		special_ = "";
		ruleSets_ = new ArrayList<RuleSet>();
	}

	@SuppressWarnings("unchecked")
	public EscapeOStream push() {
		EscapeOStream result = new EscapeOStream(sink_);

		result.ruleSets_ = (ArrayList<RuleSet>) ruleSets_.clone();

		return result;
	}

	public void pushEscape(RuleSet rules) {
		ruleSets_.add(rules);
		mixRules();
	}

	public void popEscape() {
		ruleSets_.remove(ruleSets_.size() - 1);
		mixRules();
	}

	public EscapeOStream append(char c) {
		try {
			if (special_ == "")
				sink_.append(c);
			else {
				int i = special_.indexOf(c);

				if (i != -1)
					sink_.append(mixed_.get(i).s);
				else
					sink_.append(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return this;
	}

	public EscapeOStream append(String s) {
		append(s, this);
		return this;
	}

	public EscapeOStream append(String s, EscapeOStream rules) {
		try {
			if (rules.special_ == null) {
				sink_.append(s);
				return this;
			} else
				put(s, rules);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return this;

	}

	public EscapeOStream append(int i) {
		try {
			sink_.append(String.valueOf(i));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public void flush() {
	}

	private Appendable sink_ = null;

	private static class Entry {
		public Entry(char c_, String s_) {
			c = c_;
			s = s_;
		}

		public Entry clone() {
			return new Entry(c, s);
		}

		public char c;
		public String s = "";
	}

	private ArrayList<Entry> mixed_;
	private String special_;

	private void mixRules() {
		int ruleSetsSize = ruleSets_.size();

		if (ruleSetsSize == 0) {
			mixed_ = null;
			special_ = "";
		} else if (ruleSetsSize == 1) {
			mixed_ = standardSets_.get(ruleSets_.get(0).ordinal());
			special_ = standardSetsSpecial_.get(ruleSets_.get(0).ordinal());
		} else {
			mixed_ = new ArrayList<Entry>();
			for (int i = ruleSetsSize - 1; i >= 0; --i) {
				ArrayList<Entry> toMix = standardSets_.get(ruleSets_.get(i).ordinal());

				for (int j = 0; j < mixed_.size(); ++j)
					for (int k = 0; k < toMix.size(); ++k)
						mixed_.get(j).s = mixed_.get(j).s.replace(toMix.get(k).c + "", toMix.get(k).s);

				for (int j = 0; j < toMix.size(); ++j) {
					mixed_.add((Entry) toMix.get(j).clone());
					special_ += toMix.get(j).c;
				}
			}
		}
	}

	private void put(String s, EscapeOStream rules) {
		try {
			for (int pos = 0; pos != -1;) {
				int lastPos = pos;
				pos = StringUtils.strpbrk(s, pos, rules.special_);
				if (pos != -1) {
					char f = s.charAt(pos);

					sink_.append(s.substring(lastPos, pos));

					for (int i = 0; i < rules.mixed_.size(); ++i) {
						if (rules.mixed_.get(i).c == f) {
							sink_.append(rules.mixed_.get(i).s);
							break;
						}
					}

					pos = pos + 1;

				} else {
					sink_.append(s.substring(lastPos));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<RuleSet> ruleSets_ = new ArrayList<RuleSet>();

	private static ArrayList<String> standardSetsSpecial_ = new ArrayList<String>();
	private static ArrayList<Entry> htmlAttributeEntries_ = new ArrayList<Entry>();
	private static ArrayList<Entry> jsStringLiteralSQuoteEntries_ = new ArrayList<Entry>();
	private static ArrayList<Entry> jsStringLiteralDQuoteEntries_ = new ArrayList<Entry>();
	private static ArrayList<ArrayList<Entry>> standardSets_ = new ArrayList<ArrayList<Entry>>();

	static {
		standardSetsSpecial_.add("&\"<");
		standardSetsSpecial_.add("\\\n\r\t'");
		standardSetsSpecial_.add("\\\n\r\t\"");

		htmlAttributeEntries_.add(new Entry('&', "&amp;"));
		htmlAttributeEntries_.add(new Entry('\"', "&#34;"));
		htmlAttributeEntries_.add(new Entry('<', "&lt;"));

		jsStringLiteralSQuoteEntries_.add(new Entry('\\', "\\\\"));
		jsStringLiteralSQuoteEntries_.add(new Entry('\n', "\\n"));
		jsStringLiteralSQuoteEntries_.add(new Entry('\r', "\\r"));
		jsStringLiteralSQuoteEntries_.add(new Entry('\t', "\\t"));
		jsStringLiteralSQuoteEntries_.add(new Entry('\'', "\\'"));

		jsStringLiteralDQuoteEntries_.add(new Entry('\\', "\\\\"));
		jsStringLiteralDQuoteEntries_.add(new Entry('\n', "\\n"));
		jsStringLiteralDQuoteEntries_.add(new Entry('\r', "\\r"));
		jsStringLiteralDQuoteEntries_.add(new Entry('\t', "\\t"));
		jsStringLiteralDQuoteEntries_.add(new Entry('"', "\\\""));

		standardSets_.add(htmlAttributeEntries_);
		standardSets_.add(jsStringLiteralSQuoteEntries_);
		standardSets_.add(jsStringLiteralDQuoteEntries_);
	}
}