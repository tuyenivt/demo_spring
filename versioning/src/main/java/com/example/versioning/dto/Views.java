package com.example.versioning.dto;

public final class Views {
    private Views() {
    }

    public interface V1 {
    }

    public interface V2 extends V1 {
    }
}
