package ru.relastic.meet009;

import android.content.Context;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LSManager implements Runnable{
    public static final int RUN_TYPE_READ = 0;
    public static final int RUN_TYPE_WRITE = 1;
    public static final String LOADING_STATE_SATB = "... loading file ...";
    private  static final String FILENAME_SUFFIX = ".note";

    private final File mFileDir;
    private final FileListeners mFileListeners;
    private final String fileName;
    private final String mData;
    private final int typeProcess;

    private boolean state=false;


    LSManager(Context context, FileListeners listeners, String id,  int type, String data){
        mFileDir = context.getFilesDir();
        mFileListeners = listeners;
        fileName = id+FILENAME_SUFFIX;
        typeProcess = type;
        mData = data;
    }
    public void startWorkedThread(){


        if (!state) {
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        String retVal="";
        Boolean writed = false;
        if (typeProcess==RUN_TYPE_READ) {
            File file = new File(mFileDir,fileName);
            try {
                if (!file.exists()) {file.createNewFile();}
                FileReader fr = new FileReader(file);
                int c;
                while((c=fr.read())!=-1){
                    retVal +=((char)c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileListeners.readed(retVal);
        }else {
            File file = new File(mFileDir,fileName);
            FileWriter fw = null;
            try {
                if (!file.exists()) {file.createNewFile();}
                fw = new FileWriter(file);
                for (char c : mData.toCharArray()) {
                    fw.write((int)c);
                }
                fw.close();
                writed = true;
                System.out.println("------------ файл записан: "+mData.toCharArray());
            }catch (IOException e) {
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                e.printStackTrace();
            }
            mFileListeners.writed(writed);
        }
        state=true;
    }


    public interface FileListeners {
        public void readed(String text);
        public void writed(boolean completed);
    }
}
