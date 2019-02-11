package com.example.simplesurveysample.view

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.simplesurveysample.R
import com.example.simplesurveysample.databinding.ActivityQuestionnairBinding
import com.example.simplesurveysample.model.QuestionModel
import com.example.simplesurveysample.utils.AppConstants
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_questionnair.*
import java.util.*
import java.util.concurrent.TimeUnit

class QuestionnaireActivity : AppCompatActivity() {

    private lateinit var questionList: ArrayList<QuestionModel> // hold question data list
    private lateinit var dataBindingToView: ActivityQuestionnairBinding //UI Data binding
    private var currentQuestionId = 0

    // disposable Listeners
    private lateinit var disposableNextBtnClick: Disposable
    private lateinit var disposablePreviousBtnClick: Disposable
    private lateinit var disposableSkipBtnClick: Disposable

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        dataBindingToView = DataBindingUtil.setContentView(this, R.layout.activity_questionnair)
        loadQuestions()
        initListener()
    }

    /**
     * fun assumed to load question from data source
     */
    private fun loadQuestions() {
        questionList = ArrayList()
        questionList.let {
            currentQuestionId = 0
            updateUiByQuestionId(currentQuestionId)
        }
    }

    /**
     * fun update question change in UI
     */
    private fun updateUiByQuestionId(index: Int) {
        questionList.ifIdExit(index) {
            dataBindingToView.data = it[index]
            dataBindingToView.notifyChange()
        }
        questionList.ifIdNotExit(index) {
            Toast.makeText(this, R.string.msg_completed, Toast.LENGTH_LONG).show()
            finish()
        }
    }


    /**
     * fun initial clicks listeners using RxJava to avoid multiple tap.
     */
    private fun initListener() {
        disposableNextBtnClick = nextButton.clicks()
            .throttleFirst(
                AppConstants.THRESHOLD_TIME_MILLI_SEC,
                TimeUnit.MILLISECONDS
            ) //Enable user only one click on THRESHOLD_TIME time
            .subscribe { askNextQuestion() }

        disposablePreviousBtnClick = previousButton.clicks()
            .throttleFirst(AppConstants.THRESHOLD_TIME_MILLI_SEC, TimeUnit.MILLISECONDS)
            .subscribe { askPreviousQuestion() }

        disposableSkipBtnClick = skipCategoryButton.clicks()
            .throttleFirst(AppConstants.THRESHOLD_TIME_MILLI_SEC, TimeUnit.MILLISECONDS)
            .subscribe { skipToNxtCategory() }
    }


    override fun onDestroy() {
        disposeClicks() // Disposable will automatically dispose even without it.
        super.onDestroy()
    }

    /**
     * Dispose or cancel listeners
     */
    private fun disposeClicks() {
        disposablePreviousBtnClick.dispose()
        disposableNextBtnClick.dispose()
        disposableSkipBtnClick.dispose()
    }

    private fun askNextQuestion() {
        currentQuestionId = currentQuestionId.inc()
        updateUiByQuestionId(currentQuestionId)
    }

    private fun askPreviousQuestion() {
        questionList.hasPreviousAvailable(currentQuestionId) {
            currentQuestionId = currentQuestionId.inv()
            updateUiByQuestionId(currentQuestionId)
        }
    }

    private fun skipToNxtCategory() {
        currentQuestionId = firstQuestionOfNextCategory()
        updateUiByQuestionId(currentQuestionId)
    }

    /**
     * fun to find the next category first question,
     * assumed that question are ordered and categorized in sequential.
     */
    private fun firstQuestionOfNextCategory(): Int {
        val next4thQuestion = currentQuestionId + 4
        return next4thQuestion - currentQuestionId % 4
    }

}

private fun <E> ArrayList<E>.hasPreviousAvailable(currentQuestionId: Int, function: () -> Unit) {
    if (currentQuestionId.isInRange(1, size - 1))
        function()
}


private fun <E> ArrayList<E>.ifIdNotExit(index: Int, block: () -> Unit) {
    if (index.isNotInRange(0, size - 1)) {
        block()
    }
}

private fun <E> java.util.ArrayList<E>.ifIdExit(index: Int, block: (java.util.ArrayList<E>) -> Unit) {
    if (index > -1 && size > index) {
        block(this)
    }
}

private fun Int.isInRange(start: Int, end: Int): Boolean {
    return this in start..end
}

private fun Int.isNotInRange(start: Int, end: Int): Boolean {
    return this !in start..end
}


