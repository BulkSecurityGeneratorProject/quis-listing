package com.manev.quislisting.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Entity for storing messages between users.
 */
@Entity
@Table(name = "ql_dl_message_overview")
public class DlMessageOverview extends AbstractMessage {

}
