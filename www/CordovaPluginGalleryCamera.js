/**
 * @file Main JS Plugin file. All possible actions are defined here.
 * 
 * @author Luis Miguel Martín <lm.martinb@gmail.com>
 */



// Now, we'll define click event for every <input type="file"/>
// @TODO: Detect if fileChooser has onclick event and it's not ours (execute before ours).
document.addEventListener("deviceready", function() {

    var observer = new MutationObserver(function(mutation) {
        var fileChoosers = document.querySelectorAll("input[type=file]");

        for( var i = 0; i < fileChoosers.length; i++ ){
            fileChoosers[i].addEventListener( 'click', function(e){
                e.preventDefault();
                console.log('clicked!');
                return false;
            });
        }
    });

    var container = document.documentElement || document.body;
    var config = { attributes: true, childList: true, characterData: true,subtree: true };

    observer.observe(container, config);

}, false);

