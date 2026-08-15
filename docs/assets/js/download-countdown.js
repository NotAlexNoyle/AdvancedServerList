document$.subscribe(async () => {
    const count = document.querySelector('[data-md-component="download-countdown"]');

    async function applyCount(downloads) {
        const remaining = 100_000 - downloads;
        const formatted = new Intl.NumberFormat().format(remaining);

        var link = document.createElement('a');
        var linkText = document.createTextNode("Soon at 100k downloads...");

        if (remaining <= 0) {
            count.className = "rainbow";
            count.innerText = "Thank you for 100 THOUSAND downloads!";
            return;
        } else if (remaining == 1) {
            linkText = document.createTextNode("1 download remaining...");
        } else {
            linkText = document.createTextNode(`${formatted} downloads remaining...`)
        }

        link.appendChild(linkText);
        link.title = "Visit Modrinth";
        link.href = "https://modrinth.com/plugin/advancedserverlist";
        link.target = "_blank";
        link.rel = "nofollow";

        count.appendChild(link);
    }

    async function fetchDownloads() {
        const data = await fetch('https://api.modrinth.com/v3/project/advancedserverlist')
            .then(_ => _.json());
        
        const downloads = data.downloads

        if (downloads !== null) {
            __md_set("__downloads_count", downloads, sessionStorage);
            applyCount(downloads)
        }
        
    }

    const cached = __md_get("__downloads_count", sessionStorage);
    if (cached != null) {
        applyCount(cached);
    } else {
        fetchDownloads();
    }
})