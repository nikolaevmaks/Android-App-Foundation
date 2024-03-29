# Android-App-Foundation

<b>Example of architecture and a lot of base classes for any Android application</b>
<br/><br/>
App uses MVP architecture with custom implementation of Fragment which is simple to use unlike Android standard Fragment.
See *BaseActivity* / *BasePresenter*, *BaseFragment* / *BaseFragmentPresenter*, *RequestBase* classes.
Fragment is just a simple View + lifecycle similar to Android Fragment lifecycle.

Also I use app architecture something like Android ViewModel, but it is more customizable and flexible, you can customize everything as you want.
See *RequestBase*, *RequestsObserver*, *RequestWaitPerformer* classes.

It uses OkHttp, Moshi libraries for network and JSON parsing. There are classes for easy network response parsing. See *WebResponseHandler*'s inheritors.

App uses wrapper around Android SharedPreferences that simplifies work and adds support of "null" preference values.

It also provides classes to easily log app events to any analytics

Classes and example of easy pagination support in RecyclerView: *AdapterProductsState*, *Products*.

Explains how you can use custom annotations, such as *@Required*, *@CollectionWithoutNulls*, *@CollectionIsNotEmptyAndWithoutNulls* to validate JSON response.

*FlexibleTextView*<br/>
Custom TextView implementation which allows to answer questions about text layout, such as ellipsized text or not, maximum available lines count
depending on the available text width and height.

*ColorUtils*<br/>
Many useful methods for color adjusting
