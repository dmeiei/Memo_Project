package kr.co.lion.memo_project

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.inputmethod.InputMethodManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kr.co.lion.memo_project.databinding.ActivityEditMemoBinding
import kotlin.concurrent.thread

class EditMemoActivity : AppCompatActivity() {
    lateinit var activityEditMemoBinding: ActivityEditMemoBinding
    lateinit var memoData: MemoData


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityEditMemoBinding = ActivityEditMemoBinding.inflate(layoutInflater)
        setContentView(activityEditMemoBinding.root)


        setToolbar()
        setView()
//        memoData = intent.getParcelableExtra("memoData")!!
        // Intent로부터 메모 정보 객체를 추출한다.
        memoData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("memoData", MemoData::class.java)!!
        } else {
            intent.getParcelableExtra<MemoData>("memoData")!!
        }
        displayMemoData()
    }

    fun setToolbar(){
        activityEditMemoBinding.apply {
            toolbarEditMemo.apply {
                // 타이틀
                title = "메모 수정"
                // Back
                setNavigationIcon(R.drawable.arrow_back_24px)
                setNavigationOnClickListener {
                    setResult(RESULT_CANCELED)
                    finish()
                }
                // 메뉴
                inflateMenu(R.menu.menu_edit)
                setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.menu_edit_done -> {
                            processInputDone()
                        }
                    }
                    true
                }
            }
        }
    }
    fun setView(){
        activityEditMemoBinding.apply {

            editMemoTitle.isEnabled = true
            toolbarEditMemo.isEnabled = true

        }
    }

    fun displayMemoData(){
        activityEditMemoBinding.apply {

            editMemoTitle.setText(memoData.title)
            editMemo.setText(memoData.contents)

        }
    }

//     입력 완료 처리
    fun processInputDone(){
        // Toast.makeText(this@InputActivity, "눌러졌습니다", Toast.LENGTH_SHORT).show()

        activityEditMemoBinding.apply {
            // 사용자가 입력한 내용을 가져온다
            val title = editMemoTitle.text.toString()
            val contents = editMemo.text.toString()

            // 입력 검사
            if(title.isEmpty()){
                showDialog("제목 입력 오류", "제목을 입력해주세요", editMemoTitle)
                return
            }
            if(contents.isEmpty()){
                showDialog("내용 입력 오류", "내용을 입력해주세요", editMemo)
                return
            }

//            memoData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                intent.getParcelableExtra("memoData", MemoData::class.java)!!
//            } else {
//                intent.getParcelableExtra<MemoData>("memoData")!!
//            }

            // 기존의 메모 정보에 새로운 내용을 업데이트한다.
            val updatedMemoData = MemoData(title, memoData.date?: 0, contents)

            Snackbar.make(activityEditMemoBinding.root, "수정이 완료되었습니다", Snackbar.LENGTH_SHORT).show()
            // 이전으로 돌아간다.
            val resultIntent = Intent()
            resultIntent.putExtra("updatedMemoData", updatedMemoData)
            setResult(RESULT_OK, resultIntent)
            finish()


        }
    }
    // 다이얼로그를 보여주는 메서드
    fun showDialog(title:String, message:String, focusView: TextInputEditText){
        // 다이얼로그를 보여준다.
        val builder = MaterialAlertDialogBuilder(this@EditMemoActivity).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("확인"){ dialogInterface: DialogInterface, i: Int ->
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