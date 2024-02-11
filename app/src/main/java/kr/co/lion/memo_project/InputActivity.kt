package kr.co.lion.memo_project

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.inputmethod.InputMethodManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kr.co.lion.memo_project.databinding.ActivityInputBinding
import kotlin.concurrent.thread

class InputActivity : AppCompatActivity() {
    lateinit var activityInputBinding: ActivityInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityInputBinding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(activityInputBinding.root)

        setToolbar()
        setView()
    }

    // 툴바 설정
    fun setToolbar(){
        activityInputBinding.toolbarInput.apply {
            // 타이틀
            title = "메모 작성"
            // Back
            setNavigationIcon(R.drawable.arrow_back_24px)
            setNavigationOnClickListener {
                setResult(RESULT_CANCELED)
                finish()
            }
            // 메뉴
            inflateMenu(R.menu.menu_input)
            setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.menu_input_done -> {
                        processInputDone()
                        true
                    }

                    else -> false
                }
            }
        }
    }

    // View 설정
    fun setView(){
        activityInputBinding.apply {
            // 뷰에 포커스를 준다.
            inputMemoTitle.requestFocus()

            // 키보드를 올린다.
            // 이 때, View를 지정해야한다.
            showSoftInput(inputMemoTitle)
        }
    }

    // 입력 완료 처리
    fun processInputDone(){
        activityInputBinding.apply {
            // 사용자가 입력한 내용을 가져온다
            val title = inputMemoTitle.text.toString()
            val contents = inputMemoContents.text.toString()

            // 입력 검사
            if(title.isEmpty()){
                showDialog("제목 입력 오류", "제목을 입력해주세요", inputMemoTitle)
                return
            }
            if(contents.isEmpty()){
                showDialog("내용 입력 오류", "내용을 입력해주세요", inputMemoContents)
                return
            }

            // 입력받은 정보를 객체에 담아 준다.
            val memoData = MemoData(title, System.currentTimeMillis(), contents)

            // 스낵바 출력
            Snackbar.make(root, "등록이 완료되었습니다", Snackbar.LENGTH_SHORT).show()

            // 이전으로 돌아간다.
            val resultIntent = Intent()
            resultIntent.putExtra("memoData", memoData)
            setResult(RESULT_OK, resultIntent)
            finish()


        }
    }

    // 다이얼로그를 보여주는 메서드
    fun showDialog(title:String, message:String, focusView: TextInputEditText){
        // 다이얼로그를 보여준다.
        val builder = MaterialAlertDialogBuilder(this@InputActivity).apply {
            setTitle(title)
            setMessage(message)

            // 확인 버튼을 눌렀을 때 동작
            setPositiveButton("확인"){ _, _ ->
                // 입력이 오류일 경우 해당 입력란 초기화 다시 입력
                focusView.setText("")
                focusView.requestFocus()
                showSoftInput(focusView)
            }
        }
        builder.show()
    }

    // 포커스를 주고 키보드를 올려주는 메서드
    fun showSoftInput(focusView: TextInputEditText){
        thread {
            SystemClock.sleep(1000)
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(focusView, 0)
        }
    }
}