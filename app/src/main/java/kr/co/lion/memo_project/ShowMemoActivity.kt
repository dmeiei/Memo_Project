package kr.co.lion.memo_project

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kr.co.lion.memo_project.databinding.ActivityShowMemoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShowMemoActivity : AppCompatActivity() {
    lateinit var activityShowMemoBinding: ActivityShowMemoBinding

    // 메모 데이터
    lateinit var memoData: MemoData

    // EditMemoActivity의 런처
    lateinit var editActivityLauncher: ActivityResultLauncher<Intent>

    // 선택된 메모의 인덱스
    var  memoIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityShowMemoBinding = ActivityShowMemoBinding.inflate(layoutInflater)
        setContentView(activityShowMemoBinding.root)

        // Intent로부터 메모 인덱스를 추출
        memoIndex = intent.getIntExtra("memoIndex",-1)

        // Intent로부터 메모 정보 객체를 추출
        memoData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("memoData", MemoData::class.java)!!
        } else {
            intent.getParcelableExtra<MemoData>("memoData")!!
        }

        setToolbar()
        setView()
        initData()
        displayMemoData()


    }

   // 초기화 및 결과 처리
    fun initData(){
        val contract1 = ActivityResultContracts.StartActivityForResult()
        editActivityLauncher = registerForActivityResult(contract1){
            if(it.resultCode == RESULT_OK){
                if(it.data != null){
                    // EditMemoActivity에서 수정된 메모 데이터 가져오기
                    val updatedMemoData = it.data?.getParcelableExtra<MemoData>("updatedMemoData")

                    if (updatedMemoData != null) {
                        // 수정된 데이터로 현재 화면 갱신
                        memoData = updatedMemoData
                        displayMemoData()

                        // 수정된 데이터를 이전 화면으로 전달
                        setResult(RESULT_OK, Intent().putExtra("updatedMemoData", updatedMemoData))
                    }
                }
            }
        }
    }

    // 툴바 설정
    fun setToolbar(){
        activityShowMemoBinding.toolbarShowMemo.apply {
            // 타이틀
            title = "메모 보기"
            // Back
            setNavigationIcon(R.drawable.arrow_back_24px)
            setNavigationOnClickListener {
                // 현재 화면 종료 및 수정 결과 전달
                backButton()
            }
            //menu
            inflateMenu(R.menu.menu_show_menu)
            setOnMenuItemClickListener {
                when(it.itemId){
                    // 수정 메뉴 선택 시 EditMemoActivity 실행
                    R.id.menu_edit ->{
                        launchEditMemoActivity()
                        true
                    }

                    // 삭제 메뉴 선택 시 현재 메모 삭제
                    R.id.menu_delete ->{
                        // memoIndex가 -1이 아니면 (선택한 메모가 있다면) 아래 동작 수행
                        deleteMemo()
                        true
                    }
                    else -> false
                }

            }
        }
    }

    // 뷰 설정
    fun setView(){
        activityShowMemoBinding.apply {
            // 메모 내용은 수정 불가능하도록 설정
            showMemoTitle.isEnabled = false
            showMemoDate.isEnabled = false
            showMemoContents.isEnabled = false
        }
    }

    // 메모 데이터를 화면에 표시하는 함수
    fun displayMemoData(){
        activityShowMemoBinding.apply {
            // textField에 값 넣어 주기
            showMemoTitle.setText(memoData.title)
            showMemoDate.setText(getDateFormatted(memoData.date))
            showMemoContents.setText(memoData.contents)
        }
    }

    fun getDateFormatted(date: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(date))
    }

    fun backButton(){
        val resultIntent = Intent()
        resultIntent.putExtra("updatedMemoData", memoData)
        resultIntent.putExtra("editIndex", memoIndex)
        setResult(RESULT_OK,resultIntent )
        finish()
    }

    fun launchEditMemoActivity(){
        val intent = Intent(this@ShowMemoActivity, EditMemoActivity::class.java)
        intent.putExtra("memoData", memoData)
        intent.putExtra("editIndex",memoIndex)
        editActivityLauncher.launch(intent)
    }

    fun deleteMemo(){
        if(memoIndex != -1){
            val resultIntent = Intent()
            // "deleteMemoData" 에 memoIndex 값을 담아서 결과 Intent에 추가
            resultIntent.putExtra("deleteMemoData", memoIndex)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

}