package us.axelia.axelia.model;

import com.google.gson.annotations.SerializedName;

import java.io.File;

import us.axelia.axelia.model.AudioType;

/**
 * Created by mac on 2/11/14.
 */
public class Audio {
    @SerializedName("Url")
    private String url;
    @SerializedName("Description")
    private String description;
    @SerializedName("AudioType")
    private int audioTypeInt;
    private Enum audioType;
    private File temporaryFile;

    public int getAudioTypeInt() {
        return audioTypeInt;
    }

    public void setAudioTypeInt(int audioTypeInt) {
        this.audioTypeInt = audioTypeInt;
    }

    public File getTemporaryFile() {
        return temporaryFile;
    }

    public void setTemporaryFile(File temporaryFile) {
        this.temporaryFile = temporaryFile;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Enum getAudioType() {
        if (audioType == null) {
            audioType = AudioType.fromKey(audioTypeInt);
        }
        return audioType;
    }

    public void setAudioType(Enum audioType) {
        this.audioType = audioType;
    }
}
