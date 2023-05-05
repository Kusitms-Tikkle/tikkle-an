package com.team7.tikkle.view

import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.team7.tikkle.*
import com.team7.tikkle.data.ResponseMyPage
import com.team7.tikkle.data.ResponseProgress
import com.team7.tikkle.databinding.FragmentHomeExistenceBinding
import com.team7.tikkle.retrofit.APIS
import com.team7.tikkle.retrofit.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class HomeExistenceFragment : Fragment() {
    private lateinit var retService: APIS
    lateinit var binding: FragmentHomeExistenceBinding
    lateinit var homeActivity: HomeActivity
    val cal = Calendar.getInstance()
    val week: Int = cal.get(Calendar.DAY_OF_WEEK)

    //이달의 마지막 달
    val lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    private lateinit var viewModel: HomeViewModel
    private lateinit var homeRecyclerViewAdapter: HomeRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_existence, container, false)
        binding = FragmentHomeExistenceBinding.inflate(inflater, container, false)
        val userAccessToken = GlobalApplication.prefs.getString("userAccessToken", "")

        //nickname
        val userNickname = GlobalApplication.prefs.getString("userNickname", "")
        binding.mynickname.text = userNickname

        //retrofit
        retService = RetrofitClient
            .getRetrofitInstance()
            .create(APIS::class.java)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // RecyclerView 어댑터 초기화
        homeRecyclerViewAdapter = HomeRecyclerViewAdapter { task ->
            // 아이템 클릭 리스너 구현 (필요한 경우)
        }

        // RecyclerView 구성
        val recyclerView: RecyclerView = binding.recyclerview
        recyclerView.adapter = homeRecyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ViewModel과 RecyclerView 어댑터 연결
        viewModel.tasks.observe(viewLifecycleOwner, Observer { tasks ->
            homeRecyclerViewAdapter.updateTasks(tasks)
        })

        homeActivity = context as HomeActivity
        //calendar
        val today: String? = doDayOfWeek()


        //한달 남은 날짜 (D-day)
        var restMonth = lastDayOfMonth - cal.get(Calendar.DATE) + 1
        binding.restMonth.text = restMonth.toString()

        //progressBar
        //서버에서 progress 받아와서 수정 필요


        lifecycleScope.launch {
            try {
                val response = retService.progress(userAccessToken)
                if (response.isSuccessful) {
                    // Response body를 ResponseMyPage 타입으로 변환
                    val myProgress: ResponseProgress? = response.body()
                    Log.d("Home progress", "Progress : $myProgress")
                    var progress = myProgress?.result?.toInt()
                    val progressBar = binding.progressBar
                    progressBar.progress = progress!!.toInt()
                    binding.percent.text = progress.toString()

                } else {
                    // Error handling
                    Log.d(ContentValues.TAG, "Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                // Exception handling
                Log.e(ContentValues.TAG, "Exception: ${e.message}", e)
            }
        }

        return binding.root
    }


    private fun doDayOfWeek(): String? {

        val calendar = Calendar.getInstance()
        calendar.time = Date()

        // 오늘 날짜의 주 구하기
//        val week_of_year = calendar.get(Calendar.WEEK_OF_YEAR)
        val day_of_week = calendar.get(Calendar.DAY_OF_WEEK)

        // 월요일
        calendar.add(Calendar.DAY_OF_MONTH, (2 - day_of_week))
        val mondayDate = calendar.time
        val monday = dateDay(mondayDate)

        // 화요일
        calendar.add(Calendar.DAY_OF_MONTH, (3 - day_of_week))
        val tuesdayDate = calendar.time
        val tuesday = dateDay(tuesdayDate)

        // 수요일
        calendar.add(Calendar.DAY_OF_MONTH, (4 - day_of_week))
        val wednesdayDate = calendar.time
        val wednesday = dateDay(wednesdayDate)

        // 목요일
        calendar.add(Calendar.DAY_OF_MONTH, (5 - day_of_week))
        val thursdayDate = calendar.time
        val thursday = dateDay(thursdayDate)

        // 금요일
        calendar.add(Calendar.DAY_OF_MONTH, (6 - day_of_week))
        val fridayDate = calendar.time
        val friday = dateDay(fridayDate)

        // 토요일
        calendar.add(Calendar.DAY_OF_MONTH, (7 - day_of_week))
        val saturdayDate = calendar.time
        val saturday = dateDay(saturdayDate)

        // 일요일
        calendar.add(Calendar.DAY_OF_MONTH, (8 - day_of_week))
        val sundayDate = calendar.time
        val sunday = dateDay(sundayDate)


        //calendar
        val year = cal.get(Calendar.YEAR).toString()
        val month = (cal.get(Calendar.MONTH) + 1).toString()
        val day = cal.get(Calendar.DATE).toString()
        Log.d(ContentValues.TAG, "DailyMenuActivity - onCreate is called ${year}-${month}-${day}")

        var strWeek: String? = null
        val nWeek: Int = cal.get(Calendar.DAY_OF_WEEK)
        if (nWeek == 1) {
            strWeek = "일"
            binding.sun.setImageResource(R.drawable.ic_calendar_today)
            binding.textsun.setTextColor(Color.parseColor("#F56508"))
        } else if (nWeek == 2) {
            strWeek = "월"
            binding.mon.setImageResource(R.drawable.ic_calendar_today)
            binding.textmon.setTextColor(Color.parseColor("#F56508"))
        } else if (nWeek == 3) {
            strWeek = "화"
            binding.tue.setImageResource(R.drawable.ic_calendar_today)
            binding.texttue.setTextColor(Color.parseColor("#F56508"))
        } else if (nWeek == 4) {
            strWeek = "수"
            binding.wed.setImageResource(R.drawable.ic_calendar_today)
            binding.textwed.setTextColor(Color.parseColor("#F56508"))

        } else if (nWeek == 5) {
            strWeek = "목"
            binding.thu.setImageResource(R.drawable.ic_calendar_today)
            binding.textthu.setTextColor(Color.parseColor("#F56508"))
        } else if (nWeek == 6
        ) {
            strWeek = "금"
            binding.fri.setImageResource(R.drawable.ic_calendar_today)
            binding.textfri.setTextColor(Color.parseColor("#F56508"))
        } else if (nWeek == 7) {
            strWeek = "토"
            binding.sat.setImageResource(R.drawable.ic_calendar_today)
            binding.textsat.setTextColor(Color.parseColor("#F56508"))
        }

        return strWeek
    }

    fun dateDay(date: Date): String {
        val dayFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val day = dayFormat.format(date)
        return day
    }


}
