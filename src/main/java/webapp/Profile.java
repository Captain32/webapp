package webapp;

public class Profile {
    private String id;
    private String file_name;
    private String url;
    private String upload_date;
    private String user_id;

    public Profile(String id, String file_name, String url, String upload_date, String user_id) {
        this.id = id;
        this.file_name = file_name;
        this.url = url;
        this.upload_date = upload_date;
        this.user_id = user_id;
    }

    public String getId() {
        return id;
    }

    public String getFile_name() {
        return file_name;
    }

    public String getUrl() {
        return url;
    }

    public String getUpload_date() {
        return upload_date;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUpload_date(String upload_date) {
        this.upload_date = upload_date;
    }
}
