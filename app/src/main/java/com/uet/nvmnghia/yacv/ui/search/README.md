# Search functionality

[`SearchPreviewFragment`][1] and [`SearchDetailFragment`][2] is in fact the
same: same [layout][3], same [adapter][4]. They have to be separated because
Navigation [doesn't support][5] cycle/recursive graphs.

  [1]: ./preview/SearchPreviewFragment.kt
  [2]: ./detail/SearchDetailFragment.kt
  [3]: ../../../../../../../res/layout/fragment_search.xml
  [4]: ./SearchResultsAdapter.kt
  [5]: https://issuetracker.google.com/issues/118997479
