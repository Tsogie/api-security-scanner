import './App.css'
import { useState } from 'react'

  interface ApiEndpoint {
    path: string;
    method: string;
    hasAuth: boolean;
    risk: string;
    riskScore: number;
  }

  interface Report {
    apiTitle: string;
    apiVersion: string;
    totalEndpointCount: number;
    protectedCount: number;
    unprotectedCount: number;
    totalRiskScore: number;
    overallRiskLevel: string;
    warnings: string[] | null;
    endpoints: ApiEndpoint[] | null;
  }

  interface ScanResult {
    success: boolean;
    errorMessage: string | null;
    report: Report | null;
  }

function App() {

  const [url, setUrl] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<ScanResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  const isValidUrl = (value:string): boolean => {
    try {
      const parsed = new URL(value);
      return parsed.protocol === "http:" || parsed.protocol === "https:";
    }catch{
      return false;
    }
  };

  const handleScan = async() => {

    if(!url.trim()){
      setError("Pls enter valid URL");
      return;
    }

    if(!isValidUrl(url)){
      setError("Pls enter valid URL");
      return;
    }
   
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/scan/validate`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({specUrl: url})
      });

      if(!response.ok){
        throw new Error("Response is not ok");
      }

      const data = await response.json();
      console.log(data);
      setResult(data);

    } catch {
      setError("Error happened!")
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className='page'>
      <header>
        <h1>OpenAPI security scanner</h1>
        <p className="subtitle">Static security analysis for OpenAPI specifications</p>
      </header>
      
      <div className='info'>
        <div className='info-block'>
          <h3>What this tool does</h3>
          <ul>
            <li>Scans OpenAPI spec for security</li>
            <li>Makes report on security level</li>
            <li>Identifies missing authentication</li>
          </ul>
        </div>

        <div className='info-block'>
          <h3>What this tool does NOT do</h3>
          <ul>
            <li>Runtime testing or penetration testing</li>
            <li>Store or modify your API specifications</li>
            <li>Access your actual API endpoints</li>
          </ul>
        </div>
      </div>

      <div className='scan-section'>
        <div className='scan-box'>
          <input 
            type="text"
            placeholder="Enter OpenAPI specification URL..."
            value={url}
            onChange = {(e) => setUrl(e.target.value)}
            disabled = {loading}
            />
          <button onClick={handleScan} disabled={loading || !isValidUrl(url)}>
            {loading ? "Scanning" : "Scan"}
          </button>
        </div>
      
        {error && (
          <p style={{color: "#ff6b6b"}}>
            {error}
          </p>
        )}
        {result && result.report && (
          <div className='report'>
            <h2 style={{}}>{result.report.apiTitle} v{result.report.apiVersion}</h2>
            <div className='summary'> 
              <div className='stat'>
                <span className='stat-label'>Total Endpoints</span>
                <span className='stat-num'>{result.report.totalEndpointCount}</span> 
              </div>
              <div className='stat'> 
                <span className='stat-label'>Total Unprotected Endpoints</span>
                <span className='stat-num'>{result.report.unprotectedCount}</span>
              </div>
              <div className='stat'>
                <span className='stat-label'>Total Protected Endpoints</span>
                <span className='stat-num'>{result.report.protectedCount}</span>
              </div>
            </div>
            <p style={{
              color: result.report.overallRiskLevel.startsWith('CRITICAL') ? '#ff4444'
              : result.report.overallRiskLevel.startsWith('HIGH') ? '#ff6b6b'
              : result.report.overallRiskLevel.startsWith('MEDIUM') ? '#ffa94d'
              : result.report.overallRiskLevel.startsWith('LOW') ? '#ffd43b'
               : '#10a37f'
            }}>
                Overall Risk: {result.report.overallRiskLevel}
            </p>
          </div>
        )}

        {result && result.report && result.report.warnings && (
          <div className='warnings'>
            <h3>WARNINGS</h3>
            <p>Total warnings: {result.report.warnings.length} </p>
            {result.report.warnings.map((warning, index) => (
              <p key={index} className='warning-item'>{warning}</p>
            ))}
          </div>
        )}

        {result && result.report && result.report.endpoints && (
          <div className='endpoints'>
            <h3>ENDPOINTS</h3>
            <p>Total endpoints: {result.report.endpoints.length} </p>
            <table>
              <thead>
                <tr>
                  <th>Method</th>
                  <th>Path</th>
                  <th>Auth</th>
                  <th>Risk</th>
                </tr>
              </thead>
              <tbody>
                {result.report.endpoints.map((end, index)=>(
                  <tr key={index} className={end.hasAuth ? '': 'no-auth'}>
                    <td> <span>{end.method}</span></td>
                    <td>{end.path}</td>
                    <td>{end.hasAuth? 'yes': 'none'}</td>
                    <td>{end.risk}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

export default App
