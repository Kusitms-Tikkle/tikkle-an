package com.team7.tikkle.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.team7.tikkle.GlobalApplication
import com.team7.tikkle.R
import com.team7.tikkle.data.ResponseChallengeJoin
import com.team7.tikkle.data.memoDto
import com.team7.tikkle.databinding.FragmentMemoCreateBinding
import com.team7.tikkle.retrofit.APIS
import com.team7.tikkle.retrofit.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class MemoCreateFragment : Fragment() {

    lateinit var binding : FragmentMemoCreateBinding
    lateinit var retService: APIS

    private val PICK_IMAGE_REQUEST_CODE = 1
    private val PERMISSION_REQUEST_CODE = 123

    var selectedImageUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    // 파일 액세스 권한 요청
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE)
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 획득 성공
                // 파일 접근 관련 작업 수행
            } else {
                // 권한 거부됨
                // 사용자에게 권한이 필요하다고 알릴 수 있음
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMemoCreateBinding.inflate(inflater, container, false)
        retService = RetrofitClient.getRetrofitInstance().create(APIS::class.java)

        val userAccessToken = GlobalApplication.prefs.getString("userAccessToken", "")
        val memoId = GlobalApplication.prefs.getString("memoId", "")

        // 갤러리 권한 요청
        requestPermissions()

        lifecycleScope.launch {
            try {
                val memoTitle = GlobalApplication.prefs.getString("memoTitle", "")
                val memoDate = GlobalApplication.prefs.getString("memoDate", "") // "2000-00-00"
                val month = memoDate.substring(5, 7)
                val day = memoDate.substring(8, 10)

                val localDate = LocalDate.parse(memoDate)
                val dayOfWeek = localDate.dayOfWeek
                var dayOfWeekText = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

                when (dayOfWeekText) {
                    "Monday" -> dayOfWeekText = "월요일"
                    "Tuesday" -> dayOfWeekText = "화요일"
                    "Wednesday" -> dayOfWeekText = "수요일"
                    "Thursday" -> dayOfWeekText = "목요일"
                    "Friday" -> dayOfWeekText = "금요일"
                    "Saturday" -> dayOfWeekText = "토요일"
                    "Sunday" -> dayOfWeekText = "일요일"
                    else -> dayOfWeekText = dayOfWeekText
                }

                binding.delImg.visibility = View.INVISIBLE
                binding.date.text = "$month" + "월 " +"$day" + "일 " + "$dayOfWeekText"
                binding.title.text = memoTitle


            } catch (e: Exception) { }
        }

        // Memo
        binding.memo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // 입력한 텍스트의 글자 수를 세서 표시
                val charCount = s?.length ?: 0
                binding.count.text = "$charCount/280자"
                //binding.text.visibility = View.GONE
            }
        })

        // 이미지 추가
        binding.img.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }

        // 이미지 삭제
        binding.delImg.setOnClickListener {
            binding.img.setImageResource(R.drawable.btn_memo_img)
        }

        // 저장 하기
        binding.btnSave.setOnClickListener {
            val memo = binding.memo.text.toString()

            // 메모가 작성 되었을 경우
            if (memo.count() !== null) {
                postMemo(userAccessToken, memoId, memo, selectedImageUri)

                // homeFragment 이동
//                parentFragmentManager.beginTransaction()
//                    .replace(R.id.main_frm, HomeExistenceFragment())
//                    .addToBackStack(null)
//                    .commit()
            }
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!

            // 이미지 세팅
            Glide.with(this)
                .load(selectedImageUri)
                .into(binding.img)

            binding.delImg.visibility = View.VISIBLE
        }
    }

    fun getImagePathFromUri(uri: Uri, context: Context): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            val path = cursor.getString(columnIndex)
            cursor.close()
            return path
        }
        return ""
    }

    // 메모 전송 API
    private fun postMemo(userAccessToken: String, memoNum: String, memo: String, uri: Uri?){
        val num : Int = memoNum.toInt()
        val memoDto = memoDto(memo, num)

        val gson = Gson()
        val memoDtoRequestBody = gson.toJson(memoDto).toRequestBody("application/json".toMediaTypeOrNull())

        // 이미지 선택 여부에 따라 MultipartBody.Part 생성
        val imagePart: MultipartBody.Part? = if (uri != null) {
            val imagePath = getImagePathFromUri(uri, requireContext())
            val imageFile = File(imagePath)
            val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)
        } else {
            // 이미지가 선택되지 않은 경우에 null 값으로 설정
            null
        }

        retService.memo(userAccessToken, memoDtoRequestBody, imagePart).enqueue(object :
            Callback<ResponseChallengeJoin> {
            override fun onResponse(call: Call<ResponseChallengeJoin>, response: Response<ResponseChallengeJoin>) {
                if (response.isSuccessful) {
                    val result = response.body()?.message
                    Log.d("PostMemo API : ", result.toString())

                } else {
                    Log.d("PostMemo API : ", "fail")
                }
            }
            override fun onFailure(call: Call<ResponseChallengeJoin>, t: Throwable) {
                Log.d(t.toString(), "error: ${t.toString()}")
            }
        })
    }

}