package compsci290.edu.duke.myeveryday.Listeners;

import compsci290.edu.duke.myeveryday.Models.Tag;

/**
 * Created by wangerxiao on 4/24/17.
 */

public interface TagSelectedListener {
    void onTagSelected(Tag TagSelected);
    void onEditButtionClicked(Tag TagSelected);
    void onDeleteButtionBlicked(Tag TagSelected);
}
