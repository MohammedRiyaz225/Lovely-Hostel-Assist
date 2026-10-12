# AI & NLP Architecture Specification

The AI subsystem in HostelFix runs natively inside the FastAPI application, providing real-time natural language processing without requiring costly external cloud API calls.

```
                    +------------------------------------+
                    |  Raw Complaint Text (Title + Desc) |
                    +------------------------------------+
                                      |
         +----------------------------+----------------------------+
         |                            |                            |
         v                            v                            v
+------------------+         +------------------+         +------------------+
| TF-IDF + Logistic|         | Priority Rule    |         | Cosine Similarity|
| Regression Model |         | & Heuristic      |         | Duplicate Check  |
+------------------+         +------------------+         +------------------+
         |                            |                            |
         v                            v                            v
Category & Conf %            Urgency Score                Match Flag & ID
         |                            |                            |
         +----------------------------+----------------------------+
                                      |
                                      v
                     +----------------------------------+
                     | Department Router & Summarizer   |
                     | -> Output Stored in Complaint AI |
                     +----------------------------------+
```

---

## 1. Complaint Classification (`ai/classifier.py`)
- **Algorithm**: TF-IDF Vectorizer combined with a Multinomial / Logistic Regression classifier trained on hostel domain complaints (`ai/data/complaints.csv`).
- **Target Categories**:
  - `Plumbing`: water leakage, broken taps, flush cisterns, blocked drains.
  - `Electrical`: power cuts, fan speed regulator, loose switchboards, geysers.
  - `Carpentry`: broken door hinges, almirah lock, window panes, study table.
  - `Internet / Wi-Fi`: LAN port not working, slow speed, captive portal login issues.
  - `Housekeeping`: room cleaning, dustbin emptying, corridor sanitation.
  - `Pest Control`: termites, insects, pest spray request.
- **Confidence Metric**: Computes maximum class probability. If confidence is below threshold (<0.40), flags for manual review.

---

## 2. Priority Prediction (`ai/priority.py`)
Categorizes complaints into:
- **`Critical`**: Severe safety hazards or total outages (e.g. "sparking switchboard", "electric shock", "water flooding in room", "main sewer choke").
- **`High`**: Impairs essential living conditions (e.g. "geyser not heating in winter", "fan dead in summer", "lock completely jammed").
- **`Medium`**: Standard repairs (e.g. "tap dripping slowly", "study lamp holder loose").
- **`Low`**: Minor cosmetic defects (e.g. "curtain rod bracket displaced", "chair squeak").

---

## 3. Duplicate Complaint Detection (`ai/duplicate_detection.py`)
- **Challenge**: Multiple students living on the same corridor often submit identical reports for common issues (e.g. 5 complaints for "washroom tap broken 2nd floor").
- **Solution**:
  1. Filters active complaints within the same `hostelBlock`.
  2. Builds TF-IDF character and word n-gram vectors.
  3. Calculates Cosine Similarity:
     $$\text{Cosine Similarity} = \frac{\vec{A} \cdot \vec{B}}{\|\vec{A}\| \|\vec{B}\|}$$
  4. If similarity $\ge 0.65$, marks `isDuplicate = True`, saves `duplicateOf = <parent_complaint_id>`, and attaches warning banner in both the student app and warden admin panel.

---

## 4. Sentiment Analysis & Summarization (`ai/sentiment.py`, `ai/summarizer.py`)
- **Sentiment Polarity**: Scored between $-1.0$ (highly frustrated) and $+1.0$ (positive). Allows administrators to detect escalations early.
- **Extractive Summarizer**: Pulls the core operative sentence for quick warden scan in table views.
