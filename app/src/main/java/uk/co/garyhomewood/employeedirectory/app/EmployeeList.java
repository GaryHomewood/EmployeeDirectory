package uk.co.garyhomewood.employeedirectory.app;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import uk.co.garyhomewood.employeedirectory.app.model.Employee;

public class EmployeeList extends Fragment {

    private static final String TAG = "EmployeeList";
    private ListView employeeList;
    private EmployeeListAdapter employeeListAdapter;
    private ProgressBar loader;

    public EmployeeList() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        employeeList = (ListView) rootView.findViewById(R.id.employeeList);

        loader = (ProgressBar) rootView.findViewById(R.id.loader);
        loader.setVisibility(View.GONE);

        new GetEmployees().execute();

        return rootView;
    }

    private class GetEmployees extends AsyncTask<Void, Void, List<Employee>> {

        @Override
        protected void onPreExecute() {
            loader.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Employee> doInBackground(Void... voids) {
            List<Employee> employees = new ArrayList<Employee>();

            EmployeeDirectoryApplication app = (EmployeeDirectoryApplication) getActivity().getApplication();

            List<Employee> cachedEmployees = app.getEmployees();

            if (cachedEmployees.size() == 0) {
                URL url;
                HttpURLConnection connection = null;
                String html = "";

                try {
                    url = new URL("http://www.theappbusiness.com/our-team/");
                    connection = (HttpURLConnection) url.openConnection();
                } catch (Exception ex) {
                    Log.e(TAG, "error connecting");
                }

                if (connection != null) {
                    int httpStatus;
                    try {
                        InputStream in = new BufferedInputStream(connection.getInputStream());
                        httpStatus = connection.getResponseCode();
                        html = readStream(in);
                    } catch (IOException ex) {
                        Log.e(TAG, "error streaming response");
                    } finally {
                        connection.disconnect();
                    }
                }

                if (html.length() > 0) {
                    Document doc = Jsoup.parse(html);
                    Elements employeeElements = doc.select("#users>div>div>div");

                    for (Element employeeElement : employeeElements) {
                        Employee e = new Employee();
                        e.setName(employeeElement.select("h3").text());
                        e.setPhotoUrl(employeeElement.select("div.title > img").attr("src"));
                        e.setTitle(employeeElement.select("p").first().text());
                        e.setBio(employeeElement.select("p.user-description").text());
                        employees.add(e);
                    }
                }

                app.setEmployees(employees);

                return employees;
            } else {
                return cachedEmployees;
            }
        }

        private String readStream(InputStream in) throws IOException {
            byte[] buf = new byte[1024];
            int count;
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            while ((count = in.read(buf)) != -1) {
                out.write(buf, 0, count);
            }
            return out.toString();
        }

        @Override
        protected void onPostExecute(List<Employee> employees) {
            loader.setVisibility(View.GONE);

            employeeListAdapter = new EmployeeListAdapter(getActivity(), 0, employees);

            for (Employee employee : employees) {
                employee.loadPhoto(employeeListAdapter);
            }

            employeeList.setAdapter(employeeListAdapter);
            employeeListAdapter.notifyDataSetChanged();
        }
    }

    public class EmployeeListAdapter extends ArrayAdapter<Employee> {
        private Context context;
        private List<Employee> employees;

        public EmployeeListAdapter(Context context, int resource, List<Employee> employees) {
            super(context, resource, employees);
            this.context = context;
            this.employees = employees;
        }

        @Override
        public int getCount() {
            return employees.size();
        }

        @Override
        public Employee getItem(int position) {
            return employees.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(R.layout.employee_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.title = (TextView) convertView.findViewById(R.id.title);
                viewHolder.bio = (TextView) convertView.findViewById(R.id.bio);
                viewHolder.photo = (ImageView) convertView.findViewById(R.id.photo);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Employee employee = employees.get(position);
            viewHolder.name.setText(employee.getName());
            viewHolder.title.setText(employee.getTitle());
            viewHolder.bio.setText(employee.getBio());

            if (employee.getPhoto() != null) {
                viewHolder.photo.setImageBitmap(employee.getPhoto());
            }

            return convertView;
        }
    }

    static class ViewHolder {
        TextView name;
        TextView title;
        TextView bio;
        ImageView photo;
    }
}
