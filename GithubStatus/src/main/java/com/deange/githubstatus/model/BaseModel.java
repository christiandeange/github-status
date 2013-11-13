package com.deange.githubstatus.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

public abstract class BaseModel {

    public static final String LOCAL_ID = "id";

    @DatabaseField(columnName = LOCAL_ID, generatedId = true)
    @Expose(serialize = false, deserialize = false)
    private Long mId;

    public long getId() {
        return mId;
    }

    public void setId(final long id) {
        mId = id;
    }

}
