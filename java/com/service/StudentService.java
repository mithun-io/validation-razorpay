package com.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import com.dto.Student;
import com.helper.MailHelper;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.repository.StudentRepository;

@Service
public class StudentService {

	@Autowired
	private StudentRepository repository;

	@Autowired
	MailHelper mailHelper;
	
	@Value("${razorpay.key.id}")
	private String razorpayKeyId;

	@Value("${razorpay.key.secret}")
	private String razorpayKeySecret;

	public String register(Student student, BindingResult result, ModelMap map) {
		if (!student.getPassword().equals(student.getConfirmPassword()))
			result.rejectValue("confirmPassword", "error.confirmPassword", "password and confirm password does not match");

		if (repository.existsByEmail(student.getEmail()))
			result.rejectValue("email", "error.email", "email already exists");

		if (result.hasErrors())
			return "register.html";

		else {
			repository.save(student);
			map.put("success", "registered successfully");
			
			mailHelper.sendEmail(student);
			return "register.html";
		}
	}

	public String payment(int amount, ModelMap map) throws RazorpayException {

		RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

		JSONObject orderRequest = new JSONObject();
		orderRequest.put("amount", amount * 100);
		orderRequest.put("currency", "INR");

		Order order = razorpay.orders.create(orderRequest);
		map.put("key", razorpayKeyId);
		map.put("amount", amount * 100);
		map.put("orderId", order.get("id"));
		return "razorpay.html";
	}
}
