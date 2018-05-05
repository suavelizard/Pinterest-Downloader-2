package nl.juraji.pinterestdownloader.model;

import javax.enterprise.inject.Default;

/**
 * Created by Juraji on 23-4-2018.
 * Pinterest Downloader
 */
@Default
public class BoardDao extends Dao<Board> {

    public BoardDao() {
        super(Board.class);
    }
}
