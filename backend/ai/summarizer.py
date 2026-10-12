# backend/ai/summarizer.py
# Groups related complaints into a short common-issue summary

from collections import defaultdict


def summarize_complaints(complaints: list[dict]) -> str:
    """
    Takes a list of complaint dicts (each with 'title', 'description', 'hostelBlock')
    and returns a short natural-language summary of common themes.
    """
    if not complaints:
        return "No complaints to summarize."

    # Group by category
    by_category = defaultdict(list)
    for c in complaints:
        cat = c.get("ai", {}).get("category", "Other") if c.get("ai") else "Other"
        by_category[cat].append(c)

    parts = []
    for cat, group in sorted(by_category.items(), key=lambda x: -len(x[1])):
        count = len(group)
        blocks = list({c.get("hostelBlock", "?") for c in group})
        block_str = ", ".join(f"Block {b}" for b in blocks[:3])
        if count == 1:
            parts.append(f"1 {cat} complaint in {block_str}.")
        else:
            parts.append(f"{count} {cat} complaints reported across {block_str}.")

    return " ".join(parts)
