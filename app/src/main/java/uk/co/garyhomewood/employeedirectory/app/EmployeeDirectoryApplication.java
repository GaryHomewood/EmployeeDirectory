package uk.co.garyhomewood.employeedirectory.app;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import uk.co.garyhomewood.employeedirectory.app.model.Employee;

public class EmployeeDirectoryApplication extends Application {

    private List<Employee> employees = new ArrayList<Employee>();

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {

    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }
}
