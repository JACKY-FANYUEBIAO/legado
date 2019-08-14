package io.legado.app.ui.main.bookshelf

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import io.legado.app.App
import io.legado.app.base.BaseViewModel
import io.legado.app.constant.BookType
import io.legado.app.data.entities.BookGroup
import io.legado.app.model.WebBook
import kotlinx.coroutines.Dispatchers.IO

class BookshelfViewModel(application: Application) : BaseViewModel(application) {

    val updateList = arrayListOf<String>()
    val adapterNotification = MediatorLiveData<String>()

    fun saveBookGroup(group: String?) {
        if (!group.isNullOrBlank()) {
            execute {
                App.db.bookGroupDao().insert(
                    BookGroup(
                        App.db.bookGroupDao().maxId + 1,
                        group
                    )
                )
            }
        }
    }


    fun upChapterList() {
        execute {
            App.db.bookDao().getRecentRead().map { book ->
                if (book.origin != BookType.local) {
                    val bookSource = App.db.bookSourceDao().getBookSource(book.origin)
                    bookSource?.let {
                        updateList.add(book.bookUrl)
                        adapterNotification.postValue(book.bookUrl)
                        WebBook(bookSource).getChapterList(book).onSuccess(IO) {
                            it?.let {
                                if (it.size > App.db.bookChapterDao().getChapterCount(book.bookUrl)) {
                                    App.db.bookChapterDao().insert(*it.toTypedArray())
                                }
                            }
                        }.onFinally {
                            updateList.remove(book.bookUrl)
                            adapterNotification.postValue(book.bookUrl)
                        }
                    }
                }
            }
        }
    }
}
