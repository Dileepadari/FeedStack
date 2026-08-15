/**
 * API URLs.
 */
r.util.url = {
  user_info: '../api/user',
  user_login: '../api/user/login',
  user_update: '../api/user',
  user_logout: '../api/user/logout',
  user_list: '../api/user/list',
  user_register: '../api/user',
  user_username_info: '../api/user/{username}',
  user_username_update: '../api/user/{username}',
  user_username_delete: '../api/user/{username}',
  job_delete: '../api/job/{id}',
  subscription_list: '../api/subscription',
  subscription_add: '../api/subscription',
  subscription_update: '../api/subscription/{id}',
  subscription_delete: '../api/subscription/{id}',
  subscription_get: '../api/subscription/{id}',
  subscription_import: '../api/subscription/import',
  subscription_export: '../api/subscription/export',
  subscription_favicon: '../api/subscription/{id}/favicon',
  subscription_sync: '../api/subscription/{id}/sync',
  category_update: '../api/category/{id}',
  category_delete: '../api/category/{id}',
  category_add: '../api/category',
  category_list: '../api/category',
  all: '../api/all',
  starred: '../api/starred',
  articlesummary:"../api/articlesummary",
  generate:"../api/generate",
  detector:"../api/detector",
  all_articles:"../api/detector/articles",
  starred_star: '../api/starred/{id}',
  article_read: '../api/article/{id}/read',
  article_unread: '../api/article/{id}/unread',
  articles_read: '../api/article/read',
  search: '../api/search/{query}',
  locale_list: '../api/locale',
  theme_list: '../api/theme',
  app_batch_reindex: '../api/app/batch/reindex',
  app_log: '../api/app/log',
  app_version: '../api/app',
  app_map_port: '../api/app/map_port',
  filter: '../api/filter',
  report_bug: '../api/bugs/report',
  bugs_report_getall: '../api/bugs/getall',
  bugs_report_delete: '../api/bugs/delete/{id}',
  bugs_report_updatestatus: '../api/bugs/updatestatus',
  bugs_report_getbyemail: '../api/bugs/getbyemail',
  myfeeds_get: '../api/myfeeds/getfeeds',
  myfeeds_create: '../api/myfeeds/create',
  myfeeds_add: '../api/myfeeds/add',
  myfeeds_display: '../api/myfeeds/display',
  myfeeds_allget: '../api/myfeeds/allget',
  myfeeds_alldisplay: '../api/myfeeds/alldisplay',
  trending: '../api/trending',
  github_tags: 'https://api.github.com/repos/sismics/reader/tags'
};

/**
 * Initialize utility module.
 */
r.util.init = function() {
  // Initialize toastmessage
  $().toastmessage({
    sticky: false,
    position : 'top-center'
  });
  
  // Initialize show/replace pattern
  $('body').on('click', '.show-pattern-button', function() {
    var show = $(this).attr('data-show');
    $(show, this).show();
    $(show + ' input[type="text"]:first', this).focus();
    $(this).hide();
  });
};

/**
 * On-screen replacement for window.confirm().
 * Native dialogs block the whole page, so everything here is rendered in the document.
 *
 * @param message Message to show
 * @param onConfirm Called when the user confirms
 * @param onCancel Called when the user cancels (optional)
 */
r.util.confirm = function(message, onConfirm, onCancel) {
  $('#confirm-overlay').remove();

  var overlay = $('<div id="confirm-overlay"></div>').css({
    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
    background: 'rgba(0, 0, 0, 0.45)', zIndex: 10000,
    display: 'flex', alignItems: 'center', justifyContent: 'center'
  });

  var box = $('<div class="confirm-box"></div>').css({
    background: '#fff', color: '#333', borderRadius: '4px',
    padding: '20px 24px', maxWidth: '420px', minWidth: '260px',
    boxShadow: '0 2px 12px rgba(0, 0, 0, 0.3)', textAlign: 'center'
  });

  $('<p></p>').text(message).css({ margin: '0 0 18px 0' }).appendTo(box);

  var buttons = $('<div></div>').appendTo(box);
  var cancelButton = $('<button type="button" class="btn"></button>')
      .text($.t('confirm.cancel') === 'confirm.cancel' ? 'Cancel' : $.t('confirm.cancel'))
      .css({ margin: '0 6px' });
  var okButton = $('<button type="button" class="btn btn-primary"></button>')
      .text($.t('confirm.ok') === 'confirm.ok' ? 'OK' : $.t('confirm.ok'))
      .css({ margin: '0 6px' });

  var close = function() {
    $(document).off('keydown.confirm');
    overlay.remove();
  };

  okButton.click(function() {
    close();
    if (onConfirm) {
      onConfirm();
    }
  });

  cancelButton.click(function() {
    close();
    if (onCancel) {
      onCancel();
    }
  });

  // Escape cancels, matching the native dialog.
  $(document).on('keydown.confirm', function(e) {
    if (e.which === 27) {
      cancelButton.click();
    }
  });

  buttons.append(cancelButton).append(okButton);
  overlay.append(box).appendTo('body');
  okButton.focus();
};

/**
 * Wrapper around $.ajax().
 */
r.util.ajax = function(args) {
  args.dataType = 'json';
  args.cache = false;
  if (!args.fail) {
    args.fail = function(jqxhr) {
      if (jqxhr.responseText) {
        console.log(jqxhr.responseText);
      }
      $().toastmessage('showErrorToast', $.t('error.unknown'));
    }
  }
  
  return $.ajax(args)
    .done(args.done)
    .fail(args.fail)
    .always(args.always);
};

/**
 * Returns animated CSS3 loader.
 */
r.util.buildLoader = function() {
  return '<div class="loader"><div id="bowlG"><div id="bowl_ringG"><div class="ball_holderG"><div class="ballG"></div></div></div></div></div>';
};

/**
 * Escape HTML.
 */
r.util.escape = function(str) {
  return $('<div />').text(str).html();
};

/**
 * Redraw an element (WebKit workaround).
 */
jQuery.fn.redraw = function() {
  var _this = this;
  setTimeout(function() {
    _this.hide(0, function() {
      $(this).show();
    });
  }, 10);
  return this;
};
