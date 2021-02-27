# Search functionality

|          | [`SearchPreviewFragment`][1] | [`SearchDetailFragment`][2] |
|:---------|:-----------------------------|:----------------------------|
| Category | All categories with results  | Selected category           |
| Result   | Only 3 is shown (4 is search limit; if 4 found, show "See More") | All results |

When "See More" is shown & clicked, navigate to `SearchDetailFragment`.
`SearchPreviewFragment` and `SearchDetailFragment` are in fact the same: same
[layout][3], same [adapter][4]. They have to be separated because Navigation
[doesn't support][5] cycle/recursive graphs.

  [1]: ./preview/SearchPreviewFragment.kt
  [2]: ./detail/SearchDetailFragment.kt
  [3]: ../../../../../../../res/layout/fragment_search.xml
  [4]: ./SearchResultsAdapter.kt
  [5]: https://issuetracker.google.com/issues/118997479
