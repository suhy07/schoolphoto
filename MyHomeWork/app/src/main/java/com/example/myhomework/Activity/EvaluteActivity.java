package com.example.myhomework.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.myhomework.Bean.Evaluate;
import com.example.myhomework.Service.EvaluateService;
import com.example.myhomework.Service.UserService;
import com.example.myhomework.databinding.ActivityEvaluteBinding;

public class EvaluteActivity extends AppCompatActivity {

    ActivityEvaluteBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityEvaluteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.push.setOnClickListener(v->{
            String star=binding.ratingBar.getNumStars()+"";
            String msg=binding.msg.getText().toString();
            if(msg.equals(""))
                Toast.makeText(this,"评价内容不能为空",Toast.LENGTH_LONG).show();
            Evaluate evaluate=new Evaluate();
            evaluate.setUid(UserService.GetUid());
            evaluate.setHid(HistoricalRecordsActivity.temhistory.getHid());
            evaluate.setMsg(msg);
            evaluate.setStar(star);
            EvaluateService.addEvaluate(evaluate,this);
        });
    }
}