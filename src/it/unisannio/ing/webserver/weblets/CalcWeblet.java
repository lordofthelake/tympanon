package it.unisannio.ing.webserver.weblets;

import java.io.IOException;

import it.unisannio.ing.webserver.Config;
import it.unisannio.ing.webserver.HttpRequest;
import it.unisannio.ing.webserver.HttpResponse;
import it.unisannio.ing.webserver.Weblet;

public class CalcWeblet implements Weblet {

	@Override
	public void initialize(Config config) {
		// TODO Auto-generated method stub

	}

	@Override
	public void service(HttpRequest req, HttpResponse res) throws IOException {
		double op1 = Double.valueOf(req.getParameter("op1"));
		double op2 = Double.valueOf(req.getParameter("op2"));
		double result = 0;
		String op = req.getParameter("operation");
		if(op.equals("*")) result = op1 * op2;
		else if(op.equals("+")) result = op1 + op2;
		else if(op.equals("-")) result = op1 - op2;
		else if(op.equals("/")) result = op1 / op2;
		
		res.getWriter().println(result);
	}

}
