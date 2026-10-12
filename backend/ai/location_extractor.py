# backend/ai/location_extractor.py
# Extracts hostel block mentions (e.g. "Block 34", "B-34") from complaint text

import re


# Common patterns for LPU hostel block mentions
BLOCK_PATTERNS = [
    r'\bblock\s*[-:]?\s*(\d{1,3}[A-Z]?)\b',   # Block 34, Block 34A
    r'\bb\s*[-]\s*(\d{1,3})\b',                 # B-34
    r'\bhostel\s+(\d{1,3})\b',                  # Hostel 34
    r'\bfloor\s+(\d{1,2})\b',                   # Floor 3 (secondary)
    r'\broom\s+(\d{3,4})\b',                    # Room 302
    r'\bwing\s+([A-Z])\b',                      # Wing A, Wing B
]

COMPILED = [(re.compile(p, re.IGNORECASE), idx) for idx, p in enumerate(BLOCK_PATTERNS)]


def extract_location(text: str) -> str | None:
    """
    Returns the most informative location mention found in the text,
    or None if no block/room reference is detected.
    """
    results = []
    for pattern, priority in COMPILED:
        match = pattern.search(text)
        if match:
            results.append((priority, match.group(0)))

    if not results:
        return None

    # Return the highest-priority (lowest index = most specific) match
    results.sort(key=lambda x: x[0])
    return results[0][1].strip()
