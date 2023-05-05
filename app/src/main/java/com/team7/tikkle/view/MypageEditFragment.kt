package com.team7.tikkle.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.team7.tikkle.GlobalApplication
import com.team7.tikkle.databinding.FragmentMypageEditBinding
import com.team7.tikkle.login.MainActivity
import com.team7.tikkle.retrofit.APIS
import com.team7.tikkle.retrofit.RetrofitClient
import kotlinx.coroutines.launch


class MypageEditFragment : Fragment() {

    private lateinit var retService: APIS
    lateinit var binding: FragmentMypageEditBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMypageEditBinding.inflate(inflater, container, false)
        //retrofit
        retService = RetrofitClient
            .getRetrofitInstance()
            .create(APIS::class.java)

        val userAccessToken = GlobalApplication.prefs.getString("userAccessToken", "")
        Log.d("MypageEditFragment", "userAccessToken : $userAccessToken")

        binding.logout.setOnClickListener {
            logout(userAccessToken)
        }

        binding.accountDeletion.setOnClickListener{
            delete(userAccessToken)
        }

        return binding.root
    }

    fun logout(token: String) {
        lifecycleScope.launch {
            try {
                val response = retService.logout(token)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    // TODO: Handle successful response
                    Log.d("logout", "logout")
                    Log.d("logout", "responseBody : $responseBody")

                    Toast.makeText(activity, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)


                } else {
                    // TODO: Handle error response
                    Log.d("logout error", "logout error")
                    Toast.makeText(activity, "로그아웃에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // TODO: Handle exception
                Log.d("logout error", "logout error")
                Toast.makeText(activity, "로그아웃에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun delete(token: String) {
        lifecycleScope.launch {
            try {
                val response = retService.delete(token)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    // TODO: Handle successful response
                    Log.d("accountDeletion", "accountDeletion")
                    Log.d("accountDeletion", "accountDeletion : $responseBody")

                    Toast.makeText(activity, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)


                } else {
                    // TODO: Handle error response
                    Log.d("accountDeletion error", "accountDeletion error")
                    Toast.makeText(activity, "회원탈퇴에 실패하였습니다.1", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // TODO: Handle exception
                Log.d("accountDeletion error", "accountDeletion error")
                Toast.makeText(activity, "회원탈퇴에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}