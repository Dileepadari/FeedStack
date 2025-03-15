/**
 *  Bugs report module.
 */


r.bugsreport.reset = function() {
    // Hiding bug report form
    $('#bugsreport-container').hide();
};

r.bugsreport.report = function() {
    // Getting form values
    var email = r.user.userInfo.email;
    var description = $('#bugsreport-description').val();

    // Sending bug report
    $.ajax({
        type: 'POST',
        url: r.util.report_bug,
        data: {
            email: email,
            description: description
        },
        success: function(data) {
            // Displaying success message
            $().toastmessage('showSuccessToast', $.t('bugsreport.success'));
        },
        error: function(data) {
            // Displaying error message
            $().toastmessage('showErrorToast', $.t('bugsreport.success'));
        }
    });
};

/**
 *  Initializing the bug report module
 */
r.bugsreport.init = function() {
    // Listening for hash changes on #/bugsreport/*
    $.History.bind('/bugsreport/', function(state, target) {
        // Resetting page context
        r.main.reset();

        // Displaying bug report form
        $('#bugsreport-container').show();
        $('#toolbar > .settings').removeClass('hidden');
    });

    if (!r.user.hasBaseFunction('ADMIN')) {
        $('#bugsreport-tab-admin').remove();
    }

    if (r.user.hasBaseFunction('ADMIN')) {
        $('#bugsreport-tab-users').remove();
    }

    // Listening for form submission
    $('#bugsreport-form').submit(function() {
        r.bugsreport.report();
        return false;
    });
};
