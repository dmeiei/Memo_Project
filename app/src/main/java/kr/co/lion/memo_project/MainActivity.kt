package kr.co.lion.memo_project

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import kr.co.lion.memo_project.databinding.ActivityMainBinding
import kr.co.lion.memo_project.databinding.RowBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var activityMainBinding: ActivityMainBinding

    // InputActivity의 런처
    lateinit var inputActivityLauncher: ActivityResultLauncher<Intent>

    // ShowInfoActivity의 런처
    lateinit var showInfoActivityLauncher:ActivityResultLauncher<Intent>

    //메모리스트를 저장하는 변수
    val memoList = MemoListSingleton.memoList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setToolbar()
        setView()
        initData()

    }

    //초기 데이터 설정
    fun initData(){
        // InputActivity 런처
        val contract1 = ActivityResultContracts.StartActivityForResult()
        inputActivityLauncher = registerForActivityResult(contract1){
            // 작업 결과가 OK 라면
            if(it.resultCode == RESULT_OK){
                // 전달된 Intent객체가 있다면
                if(it.data != null){
                    // 객체를 추출한다.
                    if(Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                        val memoData = it.data?.getParcelableExtra("memoData", MemoData::class.java)
                        memoList.add(memoData!!)
                        activityMainBinding.recyclerview.adapter?.notifyDataSetChanged()
                    } else {
                        val memoData = it.data?.getParcelableExtra<MemoData>("memoData")
                        memoList.add(memoData!!)
                        activityMainBinding.recyclerview.adapter?.notifyDataSetChanged()
                    }
                }
            }
        }

        // ShowInfoActivity 런처
        val contract2 = ActivityResultContracts.StartActivityForResult()
        showInfoActivityLauncher = registerForActivityResult(contract2){
            if(it.resultCode == RESULT_OK){
                //삭제된 메모의 인덱스 받아오기
                val deleteMemoIndex = it.data?.getIntExtra("deleteMemoData", -1)

                //수정된 메모 데이터 가져오기
                val updatedMemoData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getParcelableExtra("updatedMemoData", MemoData::class.java)
                } else {
                    it.data?.getParcelableExtra<MemoData>("updatedMemoData")
                }

                //메모가 삭제된 경우
                // null과 -1 이 아닐때 동작
                if (deleteMemoIndex != null && deleteMemoIndex != -1) {
                    memoList.removeAt(deleteMemoIndex)
                    // recyclerview 갱신
                    activityMainBinding.recyclerview.adapter?.notifyDataSetChanged()
                }

                // 메모가 수정된 경우
                if (updatedMemoData != null) {
                    //수정된 메모와 날짜가 일치하는 메모의 인덱스 찾기
                    // indexofFirst 함수 : 주어진 조건을 만족하는 첫번째 요소의 인덱스를 반환
                    val position = memoList.indexOfFirst { memo -> memo.date == updatedMemoData.date }

                    // 찾은 인덱스가 유효한 경우
                    // -1 은 찾지 못했다는 의미이므로 유효할 때만 아래 동작
                    if (position != -1) {
                        //해당 인덱스의 메모 업데이트
                        memoList[position] = updatedMemoData

                        // recyclerview 갱신
                        activityMainBinding.recyclerview.adapter?.notifyItemChanged(position)
                    }
                }
            }
        }

    }

    // 툴바 설정
    fun setToolbar(){
        activityMainBinding.apply {
            toolbarMain.apply {
                // Title
                title = "메모 관리"
                //menu
                inflateMenu(R.menu.menu_main)
                // 메뉴의 리스너
                setOnMenuItemClickListener {
                    when(it.itemId){
                        // 추가 메뉴
                        R.id.menu_main_add -> {
                            // InputActivity를 실행한다.
                            val inputIntent = Intent(this@MainActivity, InputActivity::class.java)
                            inputActivityLauncher.launch(inputIntent)
                        }
                    }
                    true
                }
            }

        }
    }

    //recyclerView 설정
    fun setView(){
        activityMainBinding.apply {
            recyclerview.apply {
                adapter = RecyclerViewMainAdapter()

                layoutManager = LinearLayoutManager(this@MainActivity)

                // 구분선 설정
                val deco = MaterialDividerItemDecoration(this@MainActivity, MaterialDividerItemDecoration.VERTICAL)
                addItemDecoration(deco)
            }
        }

    }

    //날짜 변환
    fun getDateFormatted(date: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(date))
    }


    inner class RecyclerViewMainAdapter : RecyclerView.Adapter<RecyclerViewMainAdapter.ViewHolderMain>(){
        inner class ViewHolderMain(rowBinding: RowBinding) : RecyclerView.ViewHolder(rowBinding.root){
            val rowBinding:RowBinding

            init {
                this.rowBinding = rowBinding

                // 항목 클릭시 전체가 클릭이 될 수 있도록
                // 가로 세로 길이를 설정해준다.
                this.rowBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // 항목을 눌렀을 때의 리스너
                this.rowBinding.root.setOnClickListener {
                    // ShowInfoActivity를 실행한다.
                    val showInfoIntent = Intent(this@MainActivity, ShowMemoActivity::class.java)

                    // 선택한 항목 번째의 학생 객체를 Intent 에 담아준다.
                    showInfoIntent.putExtra("memoData", memoList[adapterPosition])
                    showInfoIntent.putExtra("memoIndex", adapterPosition)

                    showInfoActivityLauncher.launch(showInfoIntent)

                }
            }
        }

        // 새로운 뷰 객체 생성
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderMain {
            val rowBinding = RowBinding.inflate(layoutInflater)
            val viewHolderMain = ViewHolderMain(rowBinding)

            return viewHolderMain
        }

        //어댑터가 관리하는 데이터의 총 아이템 수를 반환
        override fun getItemCount(): Int {
            return memoList.size
        }

        // 뷰 홀더에 데이터를 바인딩
        override fun onBindViewHolder(holder: ViewHolderMain, position: Int) {
            holder.rowBinding.textViewMemoTitle.text = "${memoList[position].title}"
            holder.rowBinding.textViewDate.text = getDateFormatted(memoList[position].currentDate)
        }
    }
}