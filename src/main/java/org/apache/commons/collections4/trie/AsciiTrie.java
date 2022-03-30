package org.apache.commons.collections4.trie;

import org.agrona.AsciiSequenceView;

public class AsciiTrie<T>
        extends AbstractPatriciaTrie<AsciiSequenceView, T> {
    public AsciiTrie() {
        super(AsciiKeyAnalyser.INSTANCE);
    }
}
