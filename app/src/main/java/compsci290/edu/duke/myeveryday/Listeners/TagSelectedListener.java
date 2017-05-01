package compsci290.edu.duke.myeveryday.Listeners;

import compsci290.edu.duke.myeveryday.Models.Tag;

/**
 * Created by yx78 on 4/24/17.
 * This is an interface used for dealing with the tag user selects
 */

public interface TagSelectedListener {
    // If the whole tag row is clicked
    void onTagSelected(Tag TagSelected);
    // If the edit buttion on the tag is clicked
    void onEditButtionClicked(Tag TagSelected);
    // If the delete buttion is clicked on the tag
    void onDeleteButtonClicked(Tag TagSelected);
}

