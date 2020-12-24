# State for list comic view

This is an incomplete list of states for the list comic view in Library fragment.
An interactive one is being created [here](https://app.diagrams.net/#G1vVbMf8XX_1M9Dhzb62Zwcm9M27VDSvJg).

```mermaid
graph TD
    NR[No root] -->|Click Folder| AE{Allow Ext}
    AE -->|Allow| P{Picker}
    AE -->|Deny| NA[Not allow]
    P -->|Pick| S(Scan)
    NA -->|Click Folder| ED{Educate}
    ED -->|Yes| AEF{Allow Ext not again}
    AEF -->|Allow| P
    AEF -->|Deny| NAF(Not allow forever)
```
