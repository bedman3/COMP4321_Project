package com.comp4321Project.searchEngine.View;

public class ServerStatusView {

    private final String status;

    public ServerStatusView(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ServerStatusView{" +
                "status='" + status + '\'' +
                '}';
    }
}
