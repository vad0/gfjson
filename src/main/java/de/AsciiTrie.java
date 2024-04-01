package de;

import org.agrona.AsciiSequenceView;
import org.apache.commons.collections4.trie.AbstractPatriciaTrie;

public class AsciiTrie<T>
    extends AbstractPatriciaTrie<AsciiSequenceView, T>
{
    public AsciiTrie()
    {
        super(AsciiKeyAnalyser.INSTANCE);
    }
}
