import urllib.request
import re
from concurrent.futures import ThreadPoolExecutor
import time
import json
import sys

urls = [
    ('trapezius', 'https://fitnessprogramer.com/exercise-primary-muscle/trapezius/', 6),
    ('shoulders', 'https://fitnessprogramer.com/exercise-primary-muscle/shoulders/', 20),
    ('chest', 'https://fitnessprogramer.com/exercise-primary-muscle/chest/', 16),
    ('back', 'https://fitnessprogramer.com/exercise-primary-muscle/back/', 13),
    ('triceps', 'https://fitnessprogramer.com/exercise-primary-muscle/triceps/', 11),
    ('forearm', 'https://fitnessprogramer.com/exercise-primary-muscle/forearm/', 4),
    ('leg', 'https://fitnessprogramer.com/exercise-primary-muscle/leg/', 25),
    ('calf', 'https://fitnessprogramer.com/exercise-primary-muscle/calf/', 4)
]

page_urls = []
for cat, base, max_p in urls:
    for p in range(1, max_p + 1):
        p_url = base if p == 1 else f"{base}page/{p}/"
        page_urls.append((cat, p_url))

headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}

def fetch_page(item):
    cat, url = item
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=8) as resp:
            html = resp.read().decode('utf-8', errors='ignore')
        articles = re.findall(r'<article[^>]*id=\"exercise-\d+\"[^>]*>(.*?)</article>', html, re.DOTALL)
        results = []
        for art in articles:
            title_m = re.search(r'<h2[^>]*class=[\"\']title[\"\'][^>]*>\s*<a[^>]+href=[\"\']([^\"\']+)[\"\'][^>]*>(.*?)</a>', art, re.DOTALL)
            img_m = re.search(r'<img[^>]+(?:src|data-src)=[\"\']([^\"\']+)[\"\']', art)
            if title_m and img_m:
                t = title_m.group(2).strip()
                p_url = title_m.group(1).strip()
                i_url = img_m.group(1).strip()
                results.append({
                    'category': cat,
                    'title': t,
                    'page_url': p_url,
                    'img_url': i_url
                })
        return results
    except Exception as e:
        sys.stderr.write(f"Err {url}: {e}\n")
        return []

t0 = time.time()
with ThreadPoolExecutor(max_workers=25) as ex:
    all_res = list(ex.map(fetch_page, page_urls))

flat = [item for sub in all_res for item in sub]
print(f"Scraped {len(flat)} raw items in {time.time()-t0:.1f}s")

# Deduplicate by page_url
seen = set()
dedup = []
for item in flat:
    if item['page_url'] not in seen:
        seen.add(item['page_url'])
        dedup.append(item)

print(f"Unique exercises: {len(dedup)}")

with open('all_scraped_exercises.json', 'w', encoding='utf-8') as f:
    json.dump(dedup, f, ensure_ascii=False, indent=2)

print("Saved to all_scraped_exercises.json successfully!")
