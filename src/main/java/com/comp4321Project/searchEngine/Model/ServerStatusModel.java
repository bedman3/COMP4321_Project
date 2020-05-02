package com.comp4321Project.searchEngine.Model;

public class ServerStatusModel {

    private final String status;

    public ServerStatusModel(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ServerStatusModel{" +
                "status='" + status + '\'' +
                '}';
    }
}
