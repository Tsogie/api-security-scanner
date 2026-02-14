import { useState } from 'react'
import './App.css'

function App() {
  const [specUrl, setSpecUrl] = useState('')
  const [loading, setLoading] = useState(false)
  const [results, setResults] = useState(null)
  const [error, setError] = useState(null)

  const handleScan = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    setResults(null)

    try {
      const response = await fetch('http://localhost:8080/api/scan/validate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ specUrl: specUrl })
      })

      const data = await response.json()

      if (!data.success) {
        throw new Error(data.errorMessage || 'Scan failed')
      }

      setResults(data.report)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const getRiskColor = (level) => {
    switch(level?.toLowerCase()) {
      case 'high': return '#ff6b6b'
      case 'medium': return '#ffa500'
      case 'low': return '#51cf66'
      default: return '#888'
    }
  }

  return (
    <div className="container">
      <header>
        <h1>OpenAPI Security Scanner</h1>
        <p className="subtitle">Static security analysis for OpenAPI specifications</p>
      </header>

      <section className="info">
        <div className="info-block">
          <h3>What this does</h3>
          <ul>
            <li>Validates OpenAPI specification structure and syntax</li>
            <li>Checks security scheme definitions</li>
            <li>Identifies missing authentication requirements</li>
          </ul>
        </div>
        
        <div className="info-block">
          <h3>What this doesn't do</h3>
          <ul>
            <li>Runtime testing or penetration testing</li>
            <li>Store or modify your API specifications</li>
            <li>Access your actual API endpoints</li>
          </ul>
        </div>
      </section>

      <form className="scan-form" onSubmit={handleScan}>
        <label htmlFor="spec-url">OpenAPI Specification URL</label>
        <input
          type="url"
          id="spec-url"
          placeholder="https://example.com/openapi.json"
          value={specUrl}
          onChange={(e) => setSpecUrl(e.target.value)}
          required
          disabled={loading}
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Scanning...' : 'Scan'}
        </button>
      </form>

      {error && (
        <div className="error">
          <p>{error}</p>
        </div>
      )}

      {results && (
        <div className="results">
          <div className="report-header">
            <h2>{results.apiTitle}</h2>
            <p className="version">Version {results.apiVersion}</p>
          </div>

          <div className="risk-overview">
            <div className="risk-badge" style={{ borderColor: getRiskColor(results.overallRiskLevel) }}>
              <span className="risk-label">Overall Risk</span>
              <span className="risk-level" style={{ color: getRiskColor(results.overallRiskLevel) }}>
                {results.overallRiskLevel}
              </span>
              <span className="risk-score">Score: {results.totalRiskScore}</span>
            </div>
          </div>

          <div className="stats">
            <div className="stat-item">
              <span className="stat-value">{results.totalEndpointCount}</span>
              <span className="stat-label">Total Endpoints</span>
            </div>
            <div className="stat-item">
              <span className="stat-value" style={{ color: '#51cf66' }}>{results.protectedCount}</span>
              <span className="stat-label">Protected</span>
            </div>
            <div className="stat-item">
              <span className="stat-value" style={{ color: '#ff6b6b' }}>{results.unprotectedCount}</span>
              <span className="stat-label">Unprotected</span>
            </div>
          </div>

          {results.warnings && results.warnings.length > 0 && (
            <div className="warnings">
              <h3>Warnings</h3>
              <ul>
                {results.warnings.map((warning, index) => (
                  <li key={index}>{warning}</li>
                ))}
              </ul>
            </div>
          )}

          {results.endpoints && results.endpoints.length > 0 && (
            <div className="endpoints">
              <h3>Endpoint Details</h3>
              {results.endpoints.map((endpoint, index) => (
                <div key={index} className="endpoint-card">
                  <div className="endpoint-header">
                    <span className="method">{endpoint.method}</span>
                    <span className="path">{endpoint.path}</span>
                  </div>
                  {endpoint.security && (
                    <div className="endpoint-security">
                      Security: {endpoint.security}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default App